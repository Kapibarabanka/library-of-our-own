package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.ao3scrapper.domain.{FicType, Series, Work}
import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Error, Ao3Url, domain}
import com.kapibarabanka.kapibarabot.domain.UserFicKey
import com.kapibarabanka.kapibarabot.sqlite.services.DbService
import com.kapibarabanka.kapibarabot.tg.TgError.InaccessibleMessageError
import com.kapibarabanka.kapibarabot.tg.db
import com.kapibarabanka.kapibarabot.tg.models.*
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.tg.utils.Buttons.getButtonsForNew
import com.kapibarabanka.kapibarabot.tg.utils.{Buttons, MessageText}
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

import scala.language.postfixOps

case class NewFicStateProcessor(currentState: NewFicBotState, bot: BotWithChatId, ao3: Ao3)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):

  override def startup: UIO[Unit] =
    bot
      .sendMessage(
        MessageData(MessageText.newFic(Ao3Url.fic(currentState.ficId, currentState.ficType)), replyMarkup = getButtonsForNew)
      )
      .unit

  override def onMessage(msg: Message): UIO[BotState] = StartStateProcessor(StartBotState(), bot).onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] = {
    query.data match
      case Buttons.parseAndSave.callbackData => onSave(query)
      case _                                 => unknownCallbackQuery(query).map(_ => currentState)
  }

  private def onSave(query: CallbackQuery) =
    val action = for {
      _          <- bot.answerCallbackQuery(query, Some("Working on it..."))
      startMsg   <- tryGetMessage(query)
      logParsing <- bot.editLogText(Some(startMsg), "Parsing AO3...")
      ficFromAo3 <- getFic
      savingMsg  <- bot.editLogText(logParsing, "Saving to database...")
      flatFic <- ficFromAo3 match
        case work: Work     => db.fics.add(work)
        case series: Series => db.fics.add(series)
      record <- db.details.getOrCreateUserFic(UserFicKey(bot.chatId, flatFic.id, flatFic.ficType))
      _      <- bot.editLogText(savingMsg, "Enjoy:")
    } yield ExistingFicBotState(record, true)
    action |> sendOnErrors({
      case InaccessibleMessageError() => s"trying to update message"
      case ao3Error: Ao3Error         => s"getting fic from Ao3"
      case _                          => s"adding fic to db"
    })

  private def getFic: ZIO[Any, Ao3Error, Work | Series] =
    currentState.ficType match
      case domain.FicType.Work   => ao3.work(currentState.ficId)
      case domain.FicType.Series => ao3.series(currentState.ficId)
