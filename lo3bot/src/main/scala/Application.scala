package kapibarabanka.lo3.bot

import services.{Lo3Api, Lo3bot}

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.services.MyBotApi
import zio.*
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val runBot = for {
    myBotApi <- ZIO.service[MyBotApi]
    api      <- ZIO.service[Lo3Api]
    bot      <- ZIO.succeed(new Lo3bot(myBotApi, api))
    _        <- bot.start()
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = {
    runBot.provide(
      MyBotApi.layer(s"https://api.telegram.org/bot${AppConfig.mainBotToken}"),
      Lo3Api.live(AppConfig.dataApi),
      Scope.default
    )
  }
}
