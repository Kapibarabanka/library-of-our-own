package kapibarabanka.lo3.bot
package processors

import utils.ErrorMessage

import kapibarabanka.lo3.common.services.BotWithChatId
import zio.*

trait WithErrorHandling(bot: BotWithChatId):

  def sendOnErrors[T, R](defaultValue: T)(errorToActionName: PartialFunction[Throwable, String])(
      action: ZIO[R, Throwable, T]
  ): ZIO[R, Nothing, T] =
    action.foldZIO(
      error =>
        bot
          .sendText(ErrorMessage.fromThrowable(error, errorToActionName.applyOrElse(error, _ => "doing something")))
          .map(_ => defaultValue),
      scenario => ZIO.succeed(scenario)
    )

  def sendOnError[T, R](defaultValue: T)(actionName: String)(action: ZIO[R, Throwable, T]): ZIO[R, Nothing, T] =
    sendOnErrors(defaultValue)({ case e => actionName })(action)

  private def errorMessage(e: Throwable, actionName: String) =
    s"\nError happened while $actionName: ${e.getMessage}"
