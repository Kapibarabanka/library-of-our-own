package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3Url
import com.kapibarabanka.kapibarabot.domain.UserFicKey
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

case class StartStateProcessor(currentState: StartBotState, bot: BotWithChatId, dbOld: FanficDbOld)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):
  override def startup: UIO[Unit] = ZIO.unit

  override def onMessage(msg: Message): UIO[BotState] = for {
    maybeState <- tryParseFicLink(msg)
    nextScenario <- maybeState match
      case Some(nextState) => ZIO.succeed(nextState)
      case None => bot.sendText(s"'${msg.text}' is not parsable AO3 link, don't know what to do :c ").map(_ => currentState)
  } yield nextScenario

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = unknownCallbackQuery(query).map(_ => currentState)

  private def tryParseFicLink(msg: Message): UIO[Option[BotState]] =
    val text = msg.text.getOrElse("NO_TEXT")
    Ao3Url.tryParseFicId(text) match
      case None => ZIO.succeed(None)
      case Some((ficType, ficId)) =>
        (for {
          ficExists <- dbOld.ficIsInDb(ficId, ficType)
          nextScenario <-
            if (!ficExists)
              ZIO.succeed(NewFicBotState(text))
            else
              for {
                record <- dbOld.getOrCreateUserFic(UserFicKey(bot.chatId, ficId, ficType))
              } yield ExistingFicBotState(record, true)
        } yield nextScenario) |> sendOnError("looking for fic in Airtable") map (scenario => Some(scenario))
