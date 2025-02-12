package kapibarabanka.lo3.bot
package processors

import models.{BotState, CommentBotState, ExistingFicBotState}
import services.Lo3Api
import utils.Buttons.getButtonsForExisting
import utils.{Buttons, MessageText}

import kapibarabanka.lo3.common.models.domain.{FicDetails, Quality, UserFicKey, UserFicRecord}
import kapibarabanka.lo3.common.models.tg.MessageData
import kapibarabanka.lo3.common.models.tg.TgError.*
import kapibarabanka.lo3.common.openapi.{FicDetailsClient, KindleClient}
import kapibarabanka.lo3.common.services.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

import java.time.LocalDate

case class ExistingFicStateProcessor(currentState: ExistingFicBotState, bot: BotWithChatId)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):
  private val record = currentState.displayedFic

  override def startup: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.existingFic(record), getButtonsForExisting(record))).unit

  override def onMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState] = defaultOnMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): ZIO[Lo3Api, Nothing, BotState] =
    query.data match
      case Buttons.addToBacklog.callbackData      => patchDetails(record.details.copy(backlog = true), query)
      case Buttons.removeFromBacklog.callbackData => patchDetails(record.details.copy(backlog = false), query)

      case Buttons.markAsStartedToday.callbackData  => patchDates(key => Lo3Api.run(FicDetailsClient.startedToday(key)))(query)
      case Buttons.markAsFinishedToday.callbackData => patchDates(key => Lo3Api.run(FicDetailsClient.finishedToday(key)))(query)
      case Buttons.cancelStartedToday.callbackData =>
        patchDates(key => Lo3Api.run(FicDetailsClient.cancelStartedToday(key)))(query)
      case Buttons.cancelFinishedToday.callbackData =>
        patchDates(key => Lo3Api.run(FicDetailsClient.cancelFinishedToday(key)))(query)

      case Buttons.rateNever.callbackData     => patchDetails(record.details.copy(quality = Some(Quality.Never)), query)
      case Buttons.rateMeh.callbackData       => patchDetails(record.details.copy(quality = Some(Quality.Meh)), query)
      case Buttons.rateOk.callbackData        => patchDetails(record.details.copy(quality = Some(Quality.Ok)), query)
      case Buttons.rateNice.callbackData      => patchDetails(record.details.copy(quality = Some(Quality.Nice)), query)
      case Buttons.rateBrilliant.callbackData => patchDetails(record.details.copy(quality = Some(Quality.Brilliant)), query)

      case Buttons.rateSpicy.callbackData    => patchDetails(record.details.copy(spicy = true), query)
      case Buttons.rateNotSpicy.callbackData => patchDetails(record.details.copy(spicy = false), query)

      case Buttons.addComment.callbackData => bot.answerCallbackQuery(query).map(_ => CommentBotState(record))

      case Buttons.update.callbackData       => updateFic(query)
      case Buttons.sendToKindle.callbackData => sendToKindle(query)

      case _ => unknownCallbackQuery(query).map(_ => currentState.withoutStartup)

  private def patchDetails(newDetails: FicDetails, query: CallbackQuery) =
    patchFic(s"updating details of fic with id ${record.key}")(key => Lo3Api.run(FicDetailsClient.patchDetails(key, newDetails)))(
      query
    )

  private def patchDates(patch: UserFicKey => ZIO[Lo3Api, Throwable, UserFicRecord])(query: CallbackQuery) =
    patchFic(s"updating fic ${record.key} read dates")(patch)(query)

  private def patchFic(actionName: String)(patch: UserFicKey => ZIO[Lo3Api, Throwable, UserFicRecord])(query: CallbackQuery) =
    val action = for {
      patchedFic <- patch(record.key)
      msgData <- ZIO.succeed(
        MessageData(MessageText.existingFic(patchedFic), getButtonsForExisting(patchedFic))
      )
      _             <- bot.answerCallbackQuery(query)
      messageToEdit <- tryGetMessage(query)
      _             <- bot.editMessage(messageToEdit, msgData)
    } yield ExistingFicBotState(patchedFic, false)
    action |> sendOnErrors({
      case InaccessibleMessageError() => s"trying to update fic display message"
      case _                          => actionName
    })

  private def sendToKindle(query: CallbackQuery) =
    val action = for {
      _ <- Lo3Api.run(KindleClient.sendToKindle(record.key, true))
      _ <- bot.sendText("Sent to Kindle! You can check the progress <a href=\"https://www.amazon.com/sendtokindle\">here</a>")
      updatedRecord <- Lo3Api.run(FicDetailsClient.getUserFicByKey(record.key))
      _             <- bot.answerCallbackQuery(query)
    } yield ExistingFicBotState(updatedRecord, true)
    action |> sendOnError("sending fic to email")

  private def updateFic(query: CallbackQuery) =
    val action = for {
      updatedFic <- Lo3Api.run(FicDetailsClient.updateFic(record.key))
      _          <- bot.answerCallbackQuery(query)
    } yield ExistingFicBotState(record.copy(fic = updatedFic), true)
    action |> sendOnError("updating fic data")
