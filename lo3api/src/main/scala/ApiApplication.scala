package kapibarabanka.lo3.api

import controllers.*
import sqlite.services.Lo3Data

import kapibarabanka.lo3.api.services.ao3Info.Ao3InfoService
import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.openapi.Lo3API
import kapibarabanka.lo3.common.services.MyBotApi
import zio.*
import zio.http.*
import zio.http.Header.{AccessControlAllowOrigin, Origin}
import zio.http.Middleware.{CorsConfig, cors}
import zio.http.endpoint.openapi.SwaggerUI

object ApiApplication extends ZIOAppDefault {
  private val config: CorsConfig =
    CorsConfig(
//      allowedOrigin = {
//        case origin if origin == Origin.parse("http://127.0.0.1:5173").toOption.get =>
//          Some(AccessControlAllowOrigin.Specific(origin))
//        case _ => None
//      }
      allowedOrigin = _ => Some(AccessControlAllowOrigin.All)
    )

  private val serve = for {
    _              <- Lo3Data.init.mapError(e => Exception(e))
    ao3InfoService <- ZIO.service[Ao3InfoService]
    bot            <- ZIO.service[MyBotApi]
    client         <- ZIO.service[Client]
    controllers <- ZIO.succeed(
      List(
        UserController(client, bot),
        FicDetailsController(),
        KindleController(ao3InfoService, bot),
        FicsController(ao3InfoService, bot)
      )
    )
    swaggerRoutes <- ZIO.succeed(SwaggerUI.routes("api", Lo3API.openAPI))
    routes <- ZIO.succeed(
      controllers
        .map(c => c.routes.map(r => Routes(r) @@ cors(config)).reduce((r1, r2) => r1 ++ r2))
        .foldRight(swaggerRoutes)((l, r) => l ++ r)
    )
    _ <- Server.serve(routes)
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = serve.provide(
    Ao3InfoService.live(AppConfig.ao3Login, AppConfig.ao3Password),
    Server.defaultWithPort(8090),
    Client.default,
    Scope.default,
    MyBotApi.layer(s"https://api.telegram.org/bot${AppConfig.mainBotToken}")
  )
}
