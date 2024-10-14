package kapibarabanka.lo3.api

import ao3scrapper.Ao3

import zio.*
import zio.http.*
import zio.http.codec.*
import zio.http.codec.PathCodec.*

object Application extends ZIOAppDefault {

  def ao3Routes(ao3: Ao3) =
    Routes(
      Method.GET / "work" -> handler { (req: Request) =>
        ao3
          .work(req.queryParam("id").getOrElse(""))
          .fold(e => Response.error(status = Status.InternalServerError), work => Response.text(work.title))
      }
    )

  private val serve = for {
    ao3    <- ZIO.service[Ao3]
    routes <- ZIO.succeed(ao3Routes(ao3))
    _      <- Server.serve(routes)
  } yield ()

  def run = serve.provide(Ao3.live(AppConfig.ao3Login, AppConfig.ao3Password), Server.default)
}
