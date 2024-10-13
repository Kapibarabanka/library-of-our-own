package kapibarabanka.lo3.bot

import ao3scrapper.Ao3
import tg.Kapibarabot
import tg.services.{MyBotApi, MyBotApiImpl}

import kapibarabanka.lo3.models.ao3.Ao3Model
import zio.*
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val runBot = for {
    myBotApi <- ZIO.service[MyBotApi]
    ao3      <- ZIO.service[Ao3]
    bot      <- ZIO.succeed(new Kapibarabot(myBotApi, ao3))
    _        <- bot.start()
    _ <- ZIO.succeed(Ao3Model())
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = {
    runBot.provide(
      Ao3.live(AppConfig.ao3Login, AppConfig.ao3Password),
      MyBotApiImpl.layer(s"https://api.telegram.org/bot${AppConfig.mainBotToken}"),
      Scope.default
    )
  }
}
