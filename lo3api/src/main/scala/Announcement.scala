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
  private val announceToAll = for {
    _        <- Lo3Data.init.mapError(e => Exception(e))
    allUsers <- Lo3Data.users.getAllIds
    bot      <- ZIO.service[MyBotApi]
    _        <- Console.printLine(allUsers)
//    _ <- ZIO.collectAll(allUsers.map(id => bot.sendText(id)("""
//        |New button "Update" was added to the fic actions. Now, if you add an ongoing work or series to the bot and it gets updated later, you can press this button to update tags and word count of a fic.
//        |
//        |Please note, that series update refreshes only the list of series works, so if an individual work in series got a new chapter, you should update this work specifically, and not a whole series.
//        |""".stripMargin)))
  } yield ()

  def run: ZIO[Any, Throwable, Unit] = announceToAll.provide(
    Scope.default,
    MyBotApi.layer(s"https://api.telegram.org/bot${AppConfig.announceBotToken}")
  )
}
