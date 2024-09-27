package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.kapibarabot.tg.models.{BotState, StartBotState}
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.tg.utils.ErrorMessage
import telegramium.bots.{CallbackQuery, Message}
import zio.*

trait StateProcessor(currentState: BotState, bot: BotWithChatId) extends WithErrorHandling:
  def startup: UIO[Unit]
  def onMessage(msg: Message): UIO[BotState]
  def onCallbackQuery(query: CallbackQuery): UIO[BotState]

  def sendOnError(actionName: String)(action: IO[Throwable, BotState]): UIO[BotState] =
    sendOnError(StartBotState())(actionName)(action)

  def sendOnErrors(errorToMessage: PartialFunction[Throwable, String])(action: IO[Throwable, BotState]): UIO[BotState] =
    sendOnErrors(StartBotState())(errorToMessage)(action)

  protected def unknownCallbackQuery(query: CallbackQuery): ZIO[Any, Nothing, Unit] =
    bot.answerCallbackQuery(
      query,
      Some(ErrorMessage.invalidQuery(query.data.getOrElse("")))
    )
