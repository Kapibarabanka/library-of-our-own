package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.tg.utils.ErrorMessage
import zio.*

trait WithErrorHandling(bot: BotWithChatId):

  def sendOnErrors[T](defaultValue: T)(errorToActionName: PartialFunction[Throwable, String])(
      action: ZIO[Any, Throwable, T]
  ): UIO[T] =
    action.foldZIO(
      error =>
        bot
          .sendText(ErrorMessage.fromThrowable(error, errorToActionName.applyOrElse(error, _ => "doing something")))
          .map(_ => defaultValue),
      scenario => ZIO.succeed(scenario)
    )

  def sendOnError[T](defaultValue: T)(actionName: String)(action: ZIO[Any, Throwable, T]): UIO[T] =
    sendOnErrors(defaultValue)({ case e => actionName })(action)

  private def errorMessage(e: Throwable, actionName: String) =
    s"\nError happened while $actionName: ${e.getMessage}"
