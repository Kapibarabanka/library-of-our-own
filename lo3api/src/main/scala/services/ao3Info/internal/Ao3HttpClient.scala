package kapibarabanka.lo3.api
package services.ao3Info.internal

import services.ParserApi

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.models.ao3.*
import kapibarabanka.lo3.common.parserapi.ParserClient
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import zio.*
import zio.http.*
import zio.http.Header.SetCookie
import zio.http.Header.UserAgent.ProductOrComment
import zio.http.netty.NettyConfig

trait Ao3HttpClient:
  def getFromFile(url: String, fileName: String): ZIO[Any, Ao3Error, String]
  def getFromChrome(url: String, pageType: String): ZIO[Any, Ao3Error, String]
  def get(url: String): ZIO[Any, Ao3Error, String]
  def getAuthed(url: String): ZIO[Any, Ao3Error, String]

case class Ao3HttpClientImpl(
    username: String,
    password: String,
    client: Client
) extends Ao3HttpClient:
  private val loginUrl                         = "https://archiveofourown.org/users/login"
  private var authedResponse: Option[Response] = None

  private val followRedirects     = ZClientAspect.followRedirects(2)((resp, message) => ZIO.logInfo(message).as(resp))
  private val clientWithRedirects = client @@ followRedirects

  private val cookiesFromChrome = NonEmptyChunk.fromIterable(
    Cookie.Request("user_credentials", "1"),
    List(
      Cookie.Request("_cfuvid", sys.env("_cfuvid")),
      Cookie.Request("__cf_bm", sys.env("__cf_bm")),
      Cookie.Request("cf_clearance", sys.env("cf_clearance")),
      Cookie.Request("_otwarchive_session", sys.env("_otwarchive_session"))
    )
  )

  override def getFromFile(url: String, fileName: String): ZIO[Any, Ao3Error, String] = ZIO.scoped {
    for {
      response <- client
        .request(
          Request
            .get(AppConfig.parserApi + "/downloadHtml")
            .addQueryParam("url", url)
            .addQueryParam("fileName", fileName)
        )
        .mapError(e => UnspecifiedError(e.getMessage))
      entityName <- ZIO.succeed(s"file from of $url")
      body       <- getBody(response, entityName)
    } yield body
  }

  override def getFromChrome(url: String, pageType: String): ZIO[Any, Ao3Error, String] = ZIO.scoped {
    for {
      response <- client
        .request(
          Request
            .get(AppConfig.parserApi + "/parser/source")
            .addQueryParam("url", url)
            .addQueryParam("pageType", pageType)
        )
        .mapError(e => UnspecifiedError(e.getMessage))
      entityName   <- ZIO.succeed(s"source of $url")
      body         <- getBody(response, entityName)
      existingBody <- if (body.contains("Error 404")) ZIO.fail(NotFound(entityName)) else ZIO.succeed(body)
      nonRestrictedBody <-
        if (body.contains("ERROR: restricted work")) ZIO.fail(RestrictedWork(entityName)) else ZIO.succeed(existingBody)
    } yield nonRestrictedBody
  }

  override def getAuthed(url: String): ZIO[Any, Ao3Error, String] = for {
    request <- ZIO.succeed(addAgent(Request.get(url)))
    requestWithCookies <- ZIO.succeed(
      request.addHeaders(Headers(Header.Cookie(cookiesFromChrome)))
    )
    (response, body) <- getResponseAndBody(requestWithCookies)
    _                <- ZIO.succeed(this.authedResponse = Some(response))
  } yield body

  override def get(url: String): ZIO[Any, Ao3Error, String] = for {
    request <- ZIO.succeed(authedResponse match
      case Some(response) => populateCookies(Request.get(url), response)
      case None           => addAgent(Request.get(url))
    )
    (_, body) <- getResponseAndBody(request)
  } yield body

  // TODO hopefully this is a temporary solution due to AO3 blocking parsers
//  override def getAuthed(url: String): ZIO[Any, Ao3Error, String] = for {
//    authedResponse   <- getAuthedResponse
//    _                <- ZIO.succeed(this.authedResponse = Some(authedResponse))
//    request          <- ZIO.succeed(populateCookies(addAgent(Request.get(url)), authedResponse))
//    (response, body) <- getResponseAndBody(request)
//  } yield body

  private def getAuthedResponse = for {
    (preLoginResponse, preLoginBody) <- getResponseAndBody(addAgent(Request.get(loginUrl)))
    token                            <- ZIO.succeed(getToken(preLoginBody))
    request <- ZIO.succeed(
      addAgent(
        Request
          .post(loginUrl, Body.empty)
          .addQueryParam("user[login]", username)
          .addQueryParam("user[password]", password)
          .addQueryParam("authenticity_token", token)
      )
    )
    requestWithCookies           <- ZIO.succeed(populateCookies(request, preLoginResponse))
    (authedResponse, authedBody) <- getResponseAndBody(requestWithCookies, false)
    res <-
      if (authedBody.contains("auth_error")) ZIO.fail(AuthFailed())
      else ZIO.succeed(authedResponse)
  } yield res

  private def getResponseAndBody(request: Request, allowRedirects: Boolean = true) = ZIO.scoped {
    for {
      entityName <- ZIO.succeed(s"response from ${request.url.host.getOrElse("") + request.url.path}")
      response <- (if (allowRedirects) clientWithRedirects else client)
        .request(request)
        .mapError(e => UnspecifiedError(e.getMessage))
      body <- getBody(response, entityName)
    } yield (response, body)
  }

  private def getBody(response: Response, entityName: String) = response.status match
    case Status.Found | Status.Ok =>
      response.body.asString.mapError(e => ParsingError(e.getMessage, entityName))
    case Status.TooManyRequests => ZIO.fail(TooManyRequests())
    case Status.NotFound        => ZIO.fail(NotFound(entityName))
    case status =>
      if (status.code == 525)
        ZIO.fail(Cloudflare())
      else
        ZIO.fail(HttpError(status.code, s"getting $entityName"))

  private def getToken(body: String): String = {
    val browser = JsoupBrowser()
    val soup    = browser.parseString(body)
    soup >> attr("value")("input[name=authenticity_token]")
  }

  private def populateCookies(request: Request, responseWithCookies: Response) = {
    NonEmptyChunk.fromChunk(
      responseWithCookies.headers(SetCookie).map(c => c.value.toRequest)
    ) match
      case Some(cookies) => request.addHeaders(Headers(Header.Cookie(cookies)))
      case None          => request
  }

  private def addAgent(request: Request) =
    request.addHeaders(Headers(Header.UserAgent(ProductOrComment.Product("Mozilla", Some("5.0")))))

protected[ao3Info] object Ao3HttpClientImpl {
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  private def ownLayer(username: String, password: String) = ZLayer {
    for {
      client <- ZIO.service[Client]
    } yield Ao3HttpClientImpl(username, password, client)
  }

  def layer(username: String, password: String): ZLayer[Any, Throwable, Ao3HttpClient] =
    ZLayer.make[Ao3HttpClient](
      ownLayer(username, password),
      ZLayer.succeed(clientConfig),
      Client.live,
      ZLayer.succeed(NettyConfig.default.copy(shutdownTimeoutDuration = Duration.fromSeconds(60))),
      DnsResolver.default,
      ParserApi.live(AppConfig.parserApi)
    )
}
