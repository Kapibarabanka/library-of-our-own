package com.kapibarabanka.ao3scrapper

import zio.http.*
import zio.http.netty.NettyConfig
import zio.{ZIO, ZIOAppDefault, *}

object TestZio extends ZIOAppDefault {
  private val username = sys.env("AO3_LOGIN")
  private val password = sys.env("AO3_PASSWORD")

  val config = ZClient.Config.default.idleTimeout(5.minutes)

  val program =
    for {
      work <- Ao3.work("53682058")
      _    <- Console.printLine(work.relationships.map(r => r.name))
      _    <- Console.printLine(work.characters)
    } yield ()

  override val run = program.provide(
    Ao3HttpClientImpl.layer(username, password),
    Ao3Impl.layer,
//    Client.default,
    ZLayer.succeed(config),
    Client.live,
    ZLayer.succeed(NettyConfig.default),
    DnsResolver.default
  )
}
