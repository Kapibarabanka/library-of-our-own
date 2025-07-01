package kapibarabanka.lo3.api

import controllers.*
import sqlite.services.Lo3Data
import kapibarabanka.lo3.api.services.ao3Info.Ao3InfoService

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.lo3api.Lo3API
import kapibarabanka.lo3.common.services.MyBotApi
import zio.*
import zio.http.*
import zio.http.endpoint.openapi.SwaggerUI

object Announcement extends ZIOAppDefault {
  private val text =
    """
      |Hi, I’m the creator of this bot. As you may have noticed, over the past couple of months, parsing and sending to Kindle has rarely worked due to Cloudflare errors. Ao3 is trying to protect itself from scraping for LLMs, so can't really blame them.
      |
      |However, it looks like I’ve found a workaround for these blocks that should work on the small scale of this bot. Parsing and sending should now function properly again.
      |
      |The only remaining issue is with restricted fics: you can still parse restricted works with this bot, but if a series is restricted or contains restricted works, there’s nothing I can do — the bot simply won’t be able to see them.
      |
      |If something still isn’t working, please let me know using the /feedback command. I’m currently working on turning this bot into a website, so stay tuned for more updates!
      |""".stripMargin

  private val testOnMe = for {
    _        <- Lo3Data.init.mapError(e => Exception(e))
    allUsers <- Lo3Data.users.getAllIds
    bot      <- ZIO.service[MyBotApi]
    _        <- Console.printLine(allUsers)
    _        <- bot.sendText(AppConfig.myChatId)(text)
  } yield ()
//  private val announceToAll = for {
//    _        <- Lo3Data.init.mapError(e => Exception(e))
//    allUsers <- Lo3Data.users.getAllIds
//    bot      <- ZIO.service[MyBotApi]
//    _        <- Console.printLine(allUsers)
//    _        <- ZIO.collectAll(allUsers.map(id => bot.sendText(id)(text)))
//  } yield ()

  def run: ZIO[Any, Throwable, Unit] = testOnMe.provide(
    Scope.default,
    MyBotApi.layer(s"https://api.telegram.org/bot${AppConfig.announceBotToken}")
  )
}
