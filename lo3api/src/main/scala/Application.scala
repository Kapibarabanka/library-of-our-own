package kapibarabanka.lo3.api

import ao3scrapper.Ao3
import controllers.*

import zio.*
import zio.http.*
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}

object Application extends ZIOAppDefault {
  private val serve = for {
    ao3         <- ZIO.service[Ao3]
    controllers <- ZIO.succeed(List(UserController))
    swaggerRoutes <- ZIO.succeed(
      SwaggerUI.routes(
        "api",
        OpenAPIGen.fromEndpoints(title = "Library Of Our Own API", version = "1.0", controllers.flatMap(_.endpoints))
      )
    )
    routes <- ZIO.succeed(controllers.map(c => c.routes).foldRight(swaggerRoutes)((l, r) => l ++ r))
    _      <- Server.serve(routes)
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = serve.provide(Ao3.live(AppConfig.ao3Login, AppConfig.ao3Password), Server.default)
}
