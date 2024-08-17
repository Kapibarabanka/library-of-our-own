package com.kapibarabanka.ao3scrapper

import com.kapibarabanka.ao3scrapper.exceptions.Ao3ClientError
import com.kapibarabanka.ao3scrapper.exceptions.Ao3ClientError.*
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import zio.http.*
import zio.*
import zio.http.Header.SetCookie

trait Ao3HttpClient:
  def get(url: String): ZIO[Any, Ao3ClientError, String]
  def getAuthed(url: String): ZIO[Any, Ao3ClientError, String]

case class Ao3HttpClientImpl(
    username: String,
    password: String,
    client: Client
) extends Ao3HttpClient:
  private val loginUrl = "https://archiveofourown.org/users/login"

  private val followRedirects     = ZClientAspect.followRedirects(2)((resp, message) => ZIO.logInfo(message).as(resp))
  private val clientWithRedirects = client @@ followRedirects

  override def get(url: String): ZIO[Any, Ao3ClientError, String] = for {
    (_, body) <- getResponseAndBody(Request.get(url))
  } yield body

  override def getAuthed(url: String): ZIO[Any, Ao3ClientError, String] = for {
    authedResponse   <- getAuthedResponse
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
      if (authedBody.contains("auth_error")) ZIO.fail(AuthFailed)
      else ZIO.succeed(authedResponse)
  } yield res

  /// TODO: add check for 429 and delays
  private def getResponseAndBody(request: Request, allowRedirects: Boolean = true) = ZIO.scoped {
    val withError = for {
      response <- (if (allowRedirects) clientWithRedirects else client).request(request)
      body     <- response.body.asString
    } yield (response, body)
    withError.foldZIO(
      e => {
        ZIO.fail(HttpError(e.getMessage))
      },
      s => ZIO.succeed(s)
    )
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

object Ao3HttpClientImpl {
  def layer(username: String, password: String): ZLayer[Client, Nothing, Ao3HttpClientImpl] =
    ZLayer {
      for {
        client <- ZIO.service[Client]
      } yield Ao3HttpClientImpl(username, password, client)
    }
}

object Ao3HttpClient {
  def get(url: String): ZIO[Ao3HttpClient, Ao3ClientError, String] = ZIO.serviceWithZIO[Ao3HttpClient](_.get(url))

  def getAuthed(url: String): ZIO[Ao3HttpClient, Ao3ClientError, String] =
    ZIO.serviceWithZIO[Ao3HttpClient](_.getAuthed(url))
}
