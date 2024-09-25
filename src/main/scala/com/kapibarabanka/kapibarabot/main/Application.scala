package com.kapibarabanka.kapibarabot.main

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3HttpClientImpl, Ao3Impl}
import com.kapibarabanka.kapibarabot.airtable.AirtableClient
import com.kapibarabanka.kapibarabot.services.{FicServiceImpl, UserFicServiceImpl}
import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.utils
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import telegramium.bots.high.{Api, BotApi}
import zio.*
import zio.http.netty.NettyConfig
import zio.http.{DnsResolver, ZClient, Client as ZIOClient}
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val appConfig    = utils.Config
  private val clientConfig = ZClient.Config.default.idleTimeout(5.minutes)

  private val myAppLogic = {
    BlazeClientBuilder[Task].resource
      .use { httpClient =>
        implicit val clientWithLogger: Client[Task] = Logger(logBody = true, logHeaders = true)(httpClient)
        implicit val airtable: AirtableClient       = AirtableClient(clientWithLogger, appConfig.airtableToken)
        implicit val api: Api[Task] = BotApi(clientWithLogger, baseUrl = s"https://api.telegram.org/bot${appConfig.tgToken}")
        val db = KapibarabotDb(s"${appConfig.dbPath}${appConfig.dbName}")
        (for {
          ao3 <- ZIO.service[Ao3]
          bot <- ZIO.succeed(new Kapibarabot()(ao3 = ao3))
          _   <- bot.start()
        } yield ()).provide(
          Ao3HttpClientImpl.layer(appConfig.ao3Login, appConfig.ao3Password),
          Ao3Impl.layer,
          ZLayer.succeed(clientConfig),
          ZIOClient.live,
          ZLayer.succeed(NettyConfig.default),
          DnsResolver.default,
          FicServiceImpl.layer(db),
          UserFicServiceImpl.layer(db)
        )
      }
  }

  def run = myAppLogic
}
