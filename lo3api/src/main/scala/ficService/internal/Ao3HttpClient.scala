package kapibarabanka.lo3.api
package ficService.internal

import kapibarabanka.lo3.common.models.ao3.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import zio.*
import zio.http.*
import zio.http.Header.SetCookie
import zio.http.netty.NettyConfig

trait Ao3HttpClient:
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

  override def get(url: String): ZIO[Any, Ao3Error, String] = for {
    request <- ZIO.succeed(authedResponse match
      case Some(response) => populateCookies(Request.get(url), response)
      case None           => Request.get(url)
    )
    (_, body) <- getResponseAndBody(request)
  } yield body

  override def getAuthed(url: String): ZIO[Any, Ao3Error, String] = for {
    authedResponse   <- getAuthedResponse
    _                <- ZIO.succeed(this.authedResponse = Some(authedResponse))
    request          <- ZIO.succeed(populateCookies(Request.get(url), authedResponse))
    (response, body) <- getResponseAndBody(request)
  } yield body

  private def getAuthedResponse = for {
    (preLoginResponse, preLoginBody) <- getResponseAndBody(Request.get(loginUrl))
    token                            <- ZIO.succeed(getToken(preLoginBody))
    request <- ZIO.succeed(
      Request
        .post(loginUrl, Body.empty)
        .addQueryParam("user[login]", username)
        .addQueryParam("user[password]", password)
        .addQueryParam("authenticity_token", token)
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
      body <- response.status match
        case Status.Found | Status.Ok =>
          response.body.asString.mapError(e => ParsingError(e.getMessage, entityName))
        case Status.TooManyRequests => ZIO.fail(TooManyRequests())
        case Status.NotFound        => ZIO.fail(NotFound(entityName))
        case status =>
          ZIO.fail(HttpError(status.code, s"getting $entityName"))
    } yield (response, body)
  }

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

protected[ficService] object Ao3HttpClientImpl {
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  private def ownLayer(username: String, password: String) = ZLayer {
    ZIO.service[Client].map(Ao3HttpClientImpl(username, password, _))
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
