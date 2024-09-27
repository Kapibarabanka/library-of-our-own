package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.ao3scrapper.Ao3Url
import com.kapibarabanka.kapibarabot.domain.UserFicKey
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.sqlite.services.DbService
import com.kapibarabanka.kapibarabot.tg.models.{BotState, ExistingFicBotState, NewFicBotState, StartBotState}
import com.kapibarabanka.kapibarabot.tg.utils.ErrorMessage
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

case class StartStateProcessor(currentState: StartBotState, bot: BotWithChatId, db: DbService)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):
  override def startup: UIO[Unit] = ZIO.unit

  override def onMessage(msg: Message): UIO[BotState] = for {
    maybeState <- tryParseFicLink(msg)
    nextScenario <- maybeState match
      case Some(nextState) => ZIO.succeed(nextState)
      case None =>
        bot.sendText(ErrorMessage.invalidMessage(msg.text.getOrElse(""))).map(_ => currentState)
  } yield nextScenario

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => currentState)

  private def tryParseFicLink(msg: Message): UIO[Option[BotState]] =
    Ao3Url.tryParseFicLink(msg.text.getOrElse("NO_TEXT")) match
      case None => ZIO.succeed(None)
      case Some((ficId, ficType)) =>
        val ficKey = UserFicKey(bot.chatId, ficId, ficType)
        val action = for {
          ficExists <- db.fics.ficIsInDb(ficId, ficType)
          nextScenario <-
            if (!ficExists)
              ZIO.succeed(NewFicBotState(ficId, ficType))
            else
              for {
                record <- db.details.getOrCreateUserFic(ficKey)
              } yield ExistingFicBotState(record, true)
        } yield nextScenario
        action |> sendOnError(s"getting or creating fic $ficKey in DB") map (scenario => Some(scenario))
