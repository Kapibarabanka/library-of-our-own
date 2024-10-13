package com.kapibarabanka.kapibarabot.tg.stateProcessors

import com.kapibarabanka.ao3scrapper.domain.FicType
import com.kapibarabanka.kapibarabot.domain.{FicDetails, Quality, UserFicRecord}
import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.tg.TgError.*
import com.kapibarabanka.kapibarabot.tg.db
import com.kapibarabanka.kapibarabot.tg.models.*
import com.kapibarabanka.kapibarabot.tg.services.BotWithChatId
import com.kapibarabanka.kapibarabot.tg.utils.Buttons.*
import com.kapibarabanka.kapibarabot.tg.utils.{Buttons, ErrorMessage, MessageText}
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

  override def onMessage(msg: Message): UIO[BotState] = defaultOnMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[BotState] =
    query.data match
      case Buttons.addToBacklog.callbackData      => patchDetails(record.details.copy(backlog = true), query)
      case Buttons.removeFromBacklog.callbackData => patchDetails(record.details.copy(backlog = false), query)

      case Buttons.markAsStartedToday.callbackData  => patchDates(db.details.addStartDate(_, LocalDate.now().toString))(query)
      case Buttons.markAsFinishedToday.callbackData => patchDates(db.details.addFinishDate(_, LocalDate.now().toString))(query)
      case Buttons.cancelStartedToday.callbackData  => patchDates(db.details.cancelStartedToday)(query)
      case Buttons.cancelFinishedToday.callbackData => patchDates(db.details.cancelFinishedToday)(query)

      case Buttons.rateNever.callbackData     => patchDetails(record.details.copy(quality = Some(Quality.Never)), query)
      case Buttons.rateMeh.callbackData       => patchDetails(record.details.copy(quality = Some(Quality.Meh)), query)
      case Buttons.rateOk.callbackData        => patchDetails(record.details.copy(quality = Some(Quality.Ok)), query)
      case Buttons.rateNice.callbackData      => patchDetails(record.details.copy(quality = Some(Quality.Nice)), query)
      case Buttons.rateBrilliant.callbackData => patchDetails(record.details.copy(quality = Some(Quality.Brilliant)), query)

      case Buttons.rateFire.callbackData    => patchDetails(record.details.copy(fire = true), query)
      case Buttons.rateNotFire.callbackData => patchDetails(record.details.copy(fire = false), query)

      case Buttons.addComment.callbackData => bot.answerCallbackQuery(query).map(_ => CommentBotState(record))

      case Buttons.sendToKindle.callbackData => sendToKindle(query)

      case _ => unknownCallbackQuery(query).map(_ => currentState.withoutStartup)

  private def patchDetails(newStats: FicDetails, query: CallbackQuery) =
    patchFic(s"updating details of fic with id ${record.key}")(db.details.patchFicDetails(_, newStats))(query)

  private def patchDates(patch: UserFicRecord => IO[SqliteError, UserFicRecord])(query: CallbackQuery) =
    patchFic(s"updating fic ${record.key} read dates")(patch)(query)

  private def patchFic(actionName: String)(patch: UserFicRecord => IO[SqliteError, UserFicRecord])(query: CallbackQuery) =
    val action = for {
      patchedFic <- patch(record)
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
      maybeEmail <- db.users.getKindleEmail(record.userId)
      nextState <- maybeEmail match
        case None =>
          bot
            .answerCallbackQuery(query, text = Some(ErrorMessage.noKindleEmail))
            .map(_ => currentState.withoutStartup)
        case Some(email) =>
          record.fic.ficType match
            case FicType.Work => bot.answerCallbackQuery(query).map(_ => SendToKindleBotState(record, email))
            case FicType.Series =>
              bot
                .answerCallbackQuery(query, text = Some(ErrorMessage.seriesToKindle))
                .map(_ => currentState.withoutStartup)
    } yield nextState
    action |> sendOnError("getting user Kindle email")
