package kapibarabanka.lo3.api

import controllers.*
import ficService.FicService

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.openapi.Lo3API
import kapibarabanka.lo3.common.services.MyBotApi
import zio.*
import zio.http.*
import zio.http.endpoint.openapi.SwaggerUI

object Announcment extends ZIOAppDefault {
  private val announceToAll = for {
    _        <- data.init.mapError(e => Exception(e))
    allUsers <- data.users.getAllIds
    bot      <- ZIO.service[MyBotApi]
    _ <- Console.printLine(allUsers)
//    _        <- ZIO.collectAll(allUsers.map(id => bot.sendText(id)("")))
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = announceToAll.provide(
    Scope.default,
    MyBotApi.layer(s"https://api.telegram.org/bot${AppConfig.mainBotToken}")
  )
}
