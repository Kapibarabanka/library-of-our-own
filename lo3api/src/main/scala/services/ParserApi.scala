package kapibarabanka.lo3.api
package services

import kapibarabanka.lo3.common.models.ao3.UnspecifiedError
import zio.http.*
import zio.http.endpoint.{EndpointExecutor, EndpointLocator, Invocation}
import zio.http.netty.NettyConfig
import zio.*

trait ParserApi:
  def run[P, I, TOutput, TError](
      endpoint: Invocation[P, I, TError, TOutput, zio.http.endpoint.AuthType.None]
  ): IO[TError | UnspecifiedError, TOutput]

case class ParserApiImpl(url: String, client: Client) extends ParserApi:
  private val executor = EndpointExecutor(client, EndpointLocator.fromURL(URL.decode(url).getOrElse(URL.empty)))

  def run[P, I, TOutput, TError](
      endpoint: Invocation[P, I, TError, TOutput, zio.http.endpoint.AuthType.None]
  ): IO[TError | UnspecifiedError, TOutput] =
    executor(endpoint).provide(Scope.default)

object ParserApi:
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  def live(url: String): ZLayer[Any, Throwable, ParserApi] = ZLayer.make[ParserApi](
    ZLayer {
      ZIO
        .service[Client]
        .map(client => ParserApiImpl(url, client))
    },
    ZLayer.succeed(clientConfig),
    Client.live,
    ZLayer.succeed(NettyConfig.default),
    DnsResolver.default
  )
  def run[P, I, TOutput, TError](
      endpoint: Invocation[P, I, TError, TOutput, zio.http.endpoint.AuthType.None]
  ): ZIO[ParserApi, TError | UnspecifiedError, TOutput] =
    ZIO.serviceWithZIO[ParserApi](_.run(endpoint))
