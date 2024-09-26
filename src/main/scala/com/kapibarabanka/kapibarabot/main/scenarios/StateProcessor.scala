package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.kapibarabot.services.BotWithChatId
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
      Some(s"You chose ${query.data} and I don't know what to do with it")
    )
