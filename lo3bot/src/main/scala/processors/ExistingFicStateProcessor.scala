package kapibarabanka.lo3.bot
package processors

import models.{BotState, CommentBotState, ExistingFicBotState}
import services.Lo3Api
import utils.Buttons.getButtonsForExisting
import utils.{Buttons, MessageText}

import kapibarabanka.lo3.common.models.domain.{Fic, FicDetails, UserFicKey, UserImpression}
import kapibarabanka.lo3.common.models.tg.MessageData
import kapibarabanka.lo3.common.models.tg.TgError.*
import kapibarabanka.lo3.common.lo3api.{FicDetailsClient, FicsClient, KindleClient}
import kapibarabanka.lo3.common.models.api.FinishInfo
import kapibarabanka.lo3.common.services.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

import java.time.LocalDate

case class ExistingFicStateProcessor(currentState: ExistingFicBotState, bot: BotWithChatId)
    extends StateProcessor(currentState, bot),
      WithErrorHandling(bot):
  private val fic = currentState.displayedFic

  override def startup: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.existingFic(fic), getButtonsForExisting(fic))).unit

  override def onMessage(msg: Message): ZIO[Lo3Api, Nothing, BotState] = defaultOnMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): ZIO[Lo3Api, Nothing, BotState] =
    query.data match
      case Buttons.addToBacklog.callbackData      => patchDetails(fic.details.copy(backlog = true), query)
      case Buttons.removeFromBacklog.callbackData => patchDetails(fic.details.copy(backlog = false), query)

      case Buttons.markAsStartedToday.callbackData => patchDates(key => Lo3Api.run(FicDetailsClient.startedToday(key)))(query)
      case Buttons.markAsFinishedToday.callbackData =>
        patchDates(key =>
          Lo3Api.run(FicDetailsClient.finishFic(FinishInfo(key, false, fic.details.spicy, fic.details.impression, None)))
        )(query)
      case Buttons.markAsAbandonedToday.callbackData =>
        patchDates(key =>
          Lo3Api.run(FicDetailsClient.finishFic(FinishInfo(key, true, fic.details.spicy, fic.details.impression, None)))
        )(query)

      case Buttons.rateNever.callbackData => patchDetails(fic.details.copy(impression = Some(UserImpression.Never)), query)
      case Buttons.rateMeh.callbackData   => patchDetails(fic.details.copy(impression = Some(UserImpression.Meh)), query)
      case Buttons.rateOk.callbackData    => patchDetails(fic.details.copy(impression = Some(UserImpression.Ok)), query)
      case Buttons.rateNice.callbackData  => patchDetails(fic.details.copy(impression = Some(UserImpression.Nice)), query)
      case Buttons.rateBrilliant.callbackData =>
        patchDetails(fic.details.copy(impression = Some(UserImpression.Brilliant)), query)

      case Buttons.rateSpicy.callbackData    => patchDetails(fic.details.copy(spicy = true), query)
      case Buttons.rateNotSpicy.callbackData => patchDetails(fic.details.copy(spicy = false), query)

      case Buttons.addComment.callbackData => bot.answerCallbackQuery(query).map(_ => CommentBotState(fic))

      case Buttons.syncWithAo3.callbackData       => syncWithAo3(query)
      case Buttons.sendToKindle.callbackData => sendToKindle(query)

      case _ => unknownCallbackQuery(query).map(_ => currentState.withoutStartup)

  private def patchDetails(newDetails: FicDetails, query: CallbackQuery) =
    patchFic(s"updating details of fic with id ${fic.key}")(key => Lo3Api.run(FicDetailsClient.patchDetails(key, newDetails)))(
      query
    )

  private def patchDates(patch: UserFicKey => ZIO[Lo3Api, Throwable, Unit])(query: CallbackQuery) =
    patchFic(s"updating fic ${fic.key} read dates")(patch)(query)

  private def patchFic(actionName: String)(patch: UserFicKey => ZIO[Lo3Api, Throwable, Unit])(query: CallbackQuery) =
    val action = for {
      _          <- patch(fic.key)
      patchedFic <- Lo3Api.run(FicsClient.getFicByKey(fic.key))
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
      _ <- Lo3Api.run(KindleClient.sendToKindle(fic.key, true))
      _ <- bot.sendText("Sent to Kindle! You can check the progress <a href=\"https://www.amazon.com/sendtokindle\">here</a>")
      updatedfic <- Lo3Api.run(FicsClient.getFicByKey(fic.key))
      _          <- bot.answerCallbackQuery(query)
    } yield ExistingFicBotState(updatedfic, true)
    action |> sendOnError("sending fic to email")

  private def syncWithAo3(query: CallbackQuery) =
    val action = for {
      updatedInfo <- Lo3Api.run(FicsClient.syncWithAo3(fic.key, true))
      _           <- bot.answerCallbackQuery(query)
    } yield ExistingFicBotState(fic.copy(ao3Info = updatedInfo), true)
    action |> sendOnError("updating fic data")
