package kapibarabanka.lo3.api
package services.ao3Info.internal

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.models.domain.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import zio.*
import zio.http.*
import zio.http.Header.SetCookie
import zio.http.Header.UserAgent.ProductOrComment
import zio.http.netty.NettyConfig

trait Ao3HttpClient:
  def getFromFile(url: String, fileName: String): ZIO[Any, Lo3Error, String]
  def get(url: String, pageType: PageType): ZIO[Any, Lo3Error, String]
  def toggleParser: ZIO[Any, Nothing, Boolean]

case class Ao3HttpClientImpl(
    username: String,
    password: String,
    client: Client
) extends Ao3HttpClient:
  private val loginUrl                         = "https://archiveofourown.org/users/login"
  private var authedResponse: Option[Response] = None
  private var useParser                        = true

  private val followRedirects     = ZClientAspect.followRedirects(2)((resp, message) => ZIO.logInfo(message).as(resp))
  private val clientWithRedirects = client @@ followRedirects

  override def get(url: String, pageType: PageType): ZIO[Any, Lo3Error, String] =
    if (this.useParser) then getFromChrome(url, pageType.toString.toLowerCase)
    else
      pageType match {
        case PageType.Tag => getWithoutAuth(url)
        case _            => getAuthed(url)
      }

  override def getFromFile(url: String, fileName: String): ZIO[Any, Lo3Error, String] = ZIO.scoped {
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

  override def toggleParser: ZIO[Any, Nothing, Boolean] = ZIO.succeed(this.useParser = !this.useParser).map(_ => this.useParser)

  private def getFromChrome(url: String, pageType: String): ZIO[Any, Lo3Error, String] = ZIO.scoped {
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
      existingBody <- if (body.contains("Error 404")) ZIO.fail(NotFoundOnAo3(entityName)) else ZIO.succeed(body)
      nonRestrictedBody <-
        if (body.contains("ERROR: restricted work")) ZIO.fail(RestrictedWork(entityName)) else ZIO.succeed(existingBody)
    } yield nonRestrictedBody
  }

  private def getWithoutAuth(url: String) = for {
    request <- ZIO.succeed(authedResponse match
      case Some(response) => populateCookies(Request.get(url), response)
      case None           => addAgent(Request.get(url))
    )
    (_, body) <- getResponseAndBody(request)
  } yield body

  private def getAuthed(url: String): ZIO[Any, Lo3Error, String] = for {
    authedResponse   <- getAuthedResponse
    _                <- ZIO.succeed(this.authedResponse = Some(authedResponse))
    request          <- ZIO.succeed(populateCookies(addAgent(Request.get(url)), authedResponse))
    (response, body) <- getResponseAndBody(request)
  } yield body

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
        .mapError(e => SomeAo3Error(e.getMessage))
      body <- getBody(response, entityName)
    } yield (response, body)
  }

  private def getBody(response: Response, entityName: String) = response.status match
    case Status.Found | Status.Ok =>
      response.body.asString.mapError(e => ParsingError(e.getMessage, entityName))
    case Status.TooManyRequests => ZIO.fail(TooManyRequests())
    case Status.NotFound        => ZIO.fail(NotFoundOnAo3(entityName))
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
      DnsResolver.default
    )
}
