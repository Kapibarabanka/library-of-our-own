package com.kapibarabanka.kapibarabot.tg.services

import com.kapibarabanka.kapibarabot.AppConfig
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import telegramium.bots.*
import telegramium.bots.high.BotApi
import telegramium.bots.high.implicits.*
import zio.*
import zio.interop.catz.*

object AdminBot:
  private val chatId = AppConfig.myChatId

  def feedback(chatId: String, username: Option[String], message: String) = sendMessage(s"""
       |New feedback from $chatId (${username.getOrElse("no username")}):
       |$message
       |""".stripMargin)

  def newUserAlert(chatId: String, username: Option[String]): IO[Throwable, Unit] = sendMessage(s"""
      |New bot user
      |chat id: $chatId
      |username: $username
      |""".stripMargin)

  def sendMessage(text: String): IO[Throwable, Unit] = (for {
    api <- ZIO.service[BotApi[Task]]
    _   <- telegramium.bots.high.Methods.sendMessage(chatId = ChatStrId(chatId), text = text).exec(api)
  } yield ()).provide(
    ZLayer {
      for {
        catsHttpClient       <- BlazeClientBuilder[Task].resource.toScopedZIO
        catsClientWithLogger <- ZIO.succeed(Logger(logBody = true, logHeaders = true)(catsHttpClient))
      } yield BotApi(catsClientWithLogger, baseUrl = s"https://api.telegram.org/bot${AppConfig.adminBotToken}")
    },
    Scope.default
  )
