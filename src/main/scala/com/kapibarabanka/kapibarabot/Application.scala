package com.kapibarabanka.kapibarabot

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.tg.Kapibarabot
import com.kapibarabanka.kapibarabot.tg.services.{MyBotApi, MyBotApiImpl}
import zio.*
import zio.interop.catz.*

object Application extends ZIOAppDefault {
  private val runBot = for {
    myBotApi <- ZIO.service[MyBotApi]
    ao3      <- ZIO.service[Ao3]
    bot      <- ZIO.succeed(new Kapibarabot(myBotApi, ao3))
    _        <- bot.start()
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = {
    runBot.provide(
      Ao3.live(AppConfig.ao3Login, AppConfig.ao3Password),
      MyBotApiImpl.layer(s"https://api.telegram.org/bot${AppConfig.mainBotToken}"),
      Scope.default
    )
  }
}
