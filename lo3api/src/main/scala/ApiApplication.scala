package kapibarabanka.lo3.api

import controllers.*
import ficService.FicService
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.openapi.Lo3API
import kapibarabanka.lo3.common.services.MyBotApi
import zio.*
import zio.http.*
import zio.http.endpoint.openapi.SwaggerUI

object ApiApplication extends ZIOAppDefault {
  private val serve = for {
    _          <- Lo3Data.init.mapError(e => Exception(e))
    ficService <- ZIO.service[FicService]
    bot        <- ZIO.service[MyBotApi]
    client     <- ZIO.service[Client]
    controllers <- ZIO.succeed(
      List(
        CardsController(),
        UserController(client, bot),
        FicDetailsController(ficService, bot),
        KindleController(ficService, bot)
      )
    )
    swaggerRoutes <- ZIO.succeed(SwaggerUI.routes("api", Lo3API.openAPI))
    routes <- ZIO.succeed(
      controllers.map(c => c.routes.map(r => Routes(r)).reduce((r1, r2) => r1 ++ r2)).foldRight(swaggerRoutes)((l, r) => l ++ r)
    )
    _ <- Server.serve(routes)
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = serve.provide(
    FicService.live(AppConfig.ao3Login, AppConfig.ao3Password),
    Server.defaultWithPort(8090),
    Client.default,
    Scope.default,
    MyBotApi.layer(s"https://api.telegram.org/bot${AppConfig.mainBotToken}")
  )
}
