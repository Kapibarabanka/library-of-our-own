package kapibarabanka.lo3.bot
package services

import kapibarabanka.lo3.common.models.domain.{ConnectionClosed, TooManyRequests}
import zio.http.*
import zio.http.endpoint.{EndpointExecutor, EndpointLocator, Invocation}
import zio.http.netty.NettyConfig
import zio.*

trait Lo3Api:
  def run[P, I, TOutput, TError](
      endpoint: Invocation[P, I, TError, TOutput, zio.http.endpoint.AuthType.None]
  ): IO[TError | ConnectionClosed, TOutput]

case class Lo3ApiImpl(url: String, client: Client) extends Lo3Api:
  private val executor = EndpointExecutor(client, EndpointLocator.fromURL(URL.decode(url).getOrElse(URL.empty)))

  def run[P, I, TOutput, TError](
      endpoint: Invocation[P, I, TError, TOutput, zio.http.endpoint.AuthType.None]
  ): IO[TError | ConnectionClosed, TOutput] =
    executor(endpoint).provide(Scope.default).catchAllDefect(_ => ZIO.fail(ConnectionClosed()))

object Lo3Api:
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  def live(url: String): ZLayer[Any, Throwable, Lo3Api] = ZLayer.make[Lo3Api](
    ZLayer {
      ZIO
        .service[Client]
        .map(client => Lo3ApiImpl(url, client))
    },
    ZLayer.succeed(clientConfig),
    Client.live,
    ZLayer.succeed(NettyConfig.default),
    DnsResolver.default
  )
  def run[P, I, TOutput, TError](
      endpoint: Invocation[P, I, TError, TOutput, zio.http.endpoint.AuthType.None]
  ): ZIO[Lo3Api, TError | ConnectionClosed, TOutput] =
    ZIO.serviceWithZIO[Lo3Api](_.run(endpoint))

end Lo3Api
