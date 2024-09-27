package com.kapibarabanka.kapibarabot

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3HttpClientImpl, Ao3Impl}
import com.kapibarabanka.kapibarabot.sqlite.services.*
import com.kapibarabanka.kapibarabot.tg.Kapibarabot
import com.kapibarabanka.kapibarabot.tg.services.{MyBotApi, MyBotApiImpl}
import zio.*
import zio.http.netty.NettyConfig
import zio.http.{DnsResolver, ZClient, Client as ZIOClient}
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  private val runBot = for {
    myBotApi          <- ZIO.service[MyBotApi]
    ficService        <- ZIO.service[FicService]
    ficDetailsService <- ZIO.service[FicDetailsService]
    ao3               <- ZIO.service[Ao3]
    bot               <- ZIO.succeed(new Kapibarabot(myBotApi, ao3, DbService(ficService, ficDetailsService)))
    _                 <- bot.start()
  } yield ()

  def run = {
    runBot.provide(
      Ao3HttpClientImpl.layer(AppConfig.ao3Login, AppConfig.ao3Password),
      Ao3Impl.layer,
      MyBotApiImpl.layer(s"https://api.telegram.org/bot${AppConfig.tgToken}"),
      KapibarabotDbImpl.layer(s"${AppConfig.dbPath}${AppConfig.dbName}"),
      FicServiceImpl.layer,
      FicDetailsServiceImpl.layer,
      Scope.default,
      ZLayer.succeed(clientConfig),
      ZIOClient.live,
      ZLayer.succeed(NettyConfig.default),
      DnsResolver.default
    )
  }
}
