package com.kapibarabanka.kapibarabot.main

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3HttpClientImpl, Ao3Impl}
import com.kapibarabanka.kapibarabot.services.{CatsHttpClient, CatsHttpClientImpl, MyBotApi, MyBotApiImpl}
import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.utils
import zio.*
import zio.http.netty.NettyConfig
import zio.http.{DnsResolver, ZClient, Client as ZIOClient}
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val appConfig    = utils.Config
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  private val runBot = for {
    myBotApi <- ZIO.service[MyBotApi]
    _ <- {
      for {
        ao3 <- ZIO.service[Ao3]
        bot <- ZIO.succeed(new Kapibarabot()(ao3 = ao3, bot = myBotApi))
        _   <- bot.start()
      } yield ()
    }
  } yield ()

  def run = {
    val db = KapibarabotDb(s"${appConfig.dbPath}${appConfig.dbName}")
    runBot.provide(
      Ao3HttpClientImpl.layer(appConfig.ao3Login, appConfig.ao3Password),
      Ao3Impl.layer,
      CatsHttpClientImpl.layer,
      MyBotApiImpl.layer(s"https://api.telegram.org/bot${appConfig.tgToken}"),
      ZLayer.succeed(clientConfig),
      ZIOClient.live,
      ZLayer.succeed(NettyConfig.default),
      DnsResolver.default,
//        FicServiceImpl.layer(db),
//        UserFicServiceImpl.layer(db),
      Scope.default
    )
  }
}
