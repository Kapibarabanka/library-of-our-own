package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.kapibarabot.tg.TgError
import com.kapibarabanka.kapibarabot.tg.TgError.InaccessibleMessageError
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

  protected def tryGetMessage(query: CallbackQuery): ZIO[Any, InaccessibleMessageError, Message] =
    query.message.collect { case msg: Message => msg } match
      case Some(message) => ZIO.succeed(message)
      case None          => ZIO.fail(InaccessibleMessageError())
