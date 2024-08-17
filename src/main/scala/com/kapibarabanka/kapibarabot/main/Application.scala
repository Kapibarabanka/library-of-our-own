package com.kapibarabanka.kapibarabot.main

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3HttpClientImpl, Ao3Impl}
import com.kapibarabanka.kapibarabot.bot.Kapibarabot
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import telegramium.bots.high.{Api, BotApi}
import zio.*
import zio.http.netty.NettyConfig
import zio.http.{DnsResolver, ZClient, Client as ZIOClient}
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val username      = sys.env("AO3_LOGIN")
  private val password      = sys.env("AO3_PASSWORD")
  private val airtableToken = sys.env("AIRTABLE_TOKEN")
  private val tgToken       = sys.env("TG_TOKEN")

  val config = ZClient.Config.default.idleTimeout(5.minutes)

  val myAppLogic = {
    BlazeClientBuilder[Task].resource
      .use { httpClient =>
        implicit val clientWithLogger: Client[Task] = Logger(logBody = true, logHeaders = true)(httpClient)
        implicit val airtable: AirtableClient       = AirtableClient(clientWithLogger, airtableToken)
        implicit val api: Api[Task]                 = BotApi(clientWithLogger, baseUrl = s"https://api.telegram.org/bot$tgToken")
        (for {
          ao3 <- ZIO.service[Ao3]
          bot <- ZIO.succeed(new Kapibarabot()(ao3 = ao3))
          _   <- bot.start()
        } yield ()).provide(
          Ao3HttpClientImpl.layer(username, password),
          Ao3Impl.layer,
          ZLayer.succeed(config),
          ZIOClient.live,
          ZLayer.succeed(NettyConfig.default),
          DnsResolver.default
        )
      }
  }

  def run = myAppLogic
}
