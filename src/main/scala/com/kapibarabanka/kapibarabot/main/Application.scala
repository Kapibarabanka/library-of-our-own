package com.kapibarabanka.kapibarabot.main

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3HttpClientImpl, Ao3Impl}
import com.kapibarabanka.kapibarabot.airtable.{AirtableClient, AirtableClientImpl}
import com.kapibarabanka.kapibarabot.services.{CatsHttpClient, CatsHttpClientImpl}
import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.utils
import org.http4s.client.Client as CatsClient
import telegramium.bots.high.{Api, BotApi}
import zio.*
import zio.http.netty.NettyConfig
import zio.http.{DnsResolver, ZClient, Client as ZIOClient}
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val appConfig    = utils.Config
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  private val runBot = for {
    catsClient <- ZIO.service[CatsHttpClient]
    airtable   <- ZIO.service[AirtableClient]
    _ <- {
      implicit val catsClientWithLogger: CatsClient[Task] = catsClient.http
      implicit val airtableImplicit: AirtableClient       = airtable
      implicit val api: Api[Task] = BotApi(catsClientWithLogger, baseUrl = s"https://api.telegram.org/bot${appConfig.tgToken}")
      for {
        ao3 <- ZIO.service[Ao3]
        bot <- ZIO.succeed(new Kapibarabot()(ao3 = ao3))
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
      AirtableClientImpl.layer(appConfig.airtableToken),
      ZLayer.succeed(clientConfig),
      ZIOClient.live,
      ZLayer.succeed(NettyConfig.default),
      DnsResolver.default,
//        FicServiceImpl.layer(db),
//        UserFicServiceImpl.layer(db),
      Scope.default
    )
  }

  def run1 = runBot
}
