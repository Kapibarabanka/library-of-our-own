package com.kapibarabanka.kapibarabot.main

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3HttpClientImpl, Ao3Impl}
import com.kapibarabanka.kapibarabot.services.{CatsHttpClient, CatsHttpClientImpl, DbService, FicDetailsService, FicDetailsServiceImpl, FicService, FicServiceImpl, MyBotApi, MyBotApiImpl}
import com.kapibarabanka.kapibarabot.sqlite.{KapibarabotDb, KapibarabotDbImpl}
import com.kapibarabanka.kapibarabot.utils
import zio.*
import zio.http.netty.NettyConfig
import zio.http.{DnsResolver, ZClient, Client as ZIOClient}
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val appConfig    = utils.Config
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
      Ao3HttpClientImpl.layer(appConfig.ao3Login, appConfig.ao3Password),
      Ao3Impl.layer,
      CatsHttpClientImpl.layer,
      MyBotApiImpl.layer(s"https://api.telegram.org/bot${appConfig.tgToken}"),
      KapibarabotDbImpl.layer(s"${appConfig.dbPath}${appConfig.dbName}"),
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
