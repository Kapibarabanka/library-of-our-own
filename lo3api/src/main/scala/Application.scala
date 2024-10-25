package kapibarabanka.lo3.api

import ao3scrapper.Ao3
import controllers.*

import kapibarabanka.lo3.models.openapi.Lo3API
import zio.*
import zio.http.*
import zio.http.endpoint.openapi.SwaggerUI

object Application extends ZIOAppDefault {
  private val serve = for {
    ao3         <- ZIO.service[Ao3]
    controllers <- ZIO.succeed(List(UserController))
    swaggerRoutes <- ZIO.succeed(
      SwaggerUI.routes(
        "api",
        Lo3API.openAPI
      )
    )
    routes <- ZIO.succeed(
      controllers.map(c => c.routes.map(r => Routes(r)).reduce((r1, r2) => r1 ++ r2)).foldRight(swaggerRoutes)((l, r) => l ++ r)
    )
    _ <- Server.serve(routes)
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = serve.provide(Ao3.live(AppConfig.ao3Login, AppConfig.ao3Password), Server.default)
}
