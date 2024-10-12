package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.ao3scrapper.Ao3Url
import com.kapibarabanka.ao3scrapper.domain.FicType
import com.kapibarabanka.kapibarabot.AppConfig
import com.kapibarabanka.kapibarabot.domain.{BacklogRequest, UserFicKey}
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.sqlite.services.DbService
import com.kapibarabanka.kapibarabot.tg.db
import com.kapibarabanka.kapibarabot.tg.models.{BotState, ExistingFicBotState, NewFicBotState, StartBotState}
import com.kapibarabanka.kapibarabot.tg.utils.{ErrorMessage, MessageText}
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, InputPartFile, Message}
import zio.*
import zio.http.{Body, Client, Request}
import zio.json.*

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

case class StartStateProcessor(currentState: StartBotState, bot: BotWithChatId)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot),
      WithTempFiles(bot):
  override def startup: UIO[Unit] = ZIO.unit

  override def onMessage(msg: Message): UIO[BotState] = for {
    messageText <- ZIO.succeed(msg.text.getOrElse("NO_TEXT"))
    nextState   <- getNextState(messageText)
  } yield nextState

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => currentState)

  private def getNextState(text: String): UIO[BotState] =
    text match
      case "/backlog"         => backLogCommand()
      case "/help" | "/start" => bot.sendText(MessageText.help).map(_ => currentState)
      case _ =>
        Ao3Url.tryParseFicLink(text) match
          case Some((ficId, ficType)) => getStateWithFic(ficId, ficType)
          case None                   => bot.sendText(ErrorMessage.invalidMessage(text)).map(_ => currentState)

  private def getStateWithFic(ficId: String, ficType: FicType): UIO[BotState] =
    val ficKey = UserFicKey(bot.chatId, ficId, ficType)
    val action = for {
      ficExists <- db.fics.isInDb(ficId, ficType)
      nextState <-
        if (!ficExists)
          ZIO.succeed(NewFicBotState(ficId, ficType))
        else
          for {
            record <- db.details.getOrCreateUserFic(ficKey)
          } yield ExistingFicBotState(record, true)
    } yield nextState
    action |> sendOnError(s"getting or creating fic $ficKey in DB")

  private def backLogCommand(): UIO[BotState] =
    val action = for {
      log     <- bot.editLogText(None, "Retrieving backlog...")
      records <- db.details.getUserBacklog(bot.chatId)
      log     <- bot.editLogText(log, "Generating HTML...")
      client  <- ZIO.service[Client]
      response <- client.request(
        Request.post(AppConfig.htmlApi, Body.fromString(BacklogRequest.fromRecords(records).toJson))
      )
      result <- response.body.asString
      log    <- bot.editLogText(log, "Uploading file...")
      _      <- useTempFile("backlog.html")(sendBacklog(result))
    } yield currentState
    action.provide(Client.default, Scope.default) |> sendOnError(s"getting user backlog")

  private def sendBacklog(backlog: String)(file: File) = {
    Files.write(file.toPath, backlog.getBytes(StandardCharsets.UTF_8))
    bot.sendDocument(InputPartFile(file)).unit |> sendOnError({})(s"uploading file ${file.getName}")
  }
