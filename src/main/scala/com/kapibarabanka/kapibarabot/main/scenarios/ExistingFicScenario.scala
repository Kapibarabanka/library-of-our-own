package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.FicType
import com.kapibarabanka.kapibarabot.domain.{FicDetails, Quality, UserFicKey, UserFicRecord}
import com.kapibarabanka.kapibarabot.main.BotError.*
import com.kapibarabanka.kapibarabot.main.MessageData
import com.kapibarabanka.kapibarabot.airtable.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.Buttons.*
import com.kapibarabanka.kapibarabot.utils.{BotWithChatId, Buttons, MessageText}
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

import java.time.LocalDate

case class ExistingFicScenario(record: UserFicRecord)(implicit
    bot: BotWithChatId,
    airtable: AirtableClient,
    ao3: Ao3,
    db: FanficDbOld
) extends Scenario,
      WithErrorHandling(bot):

  protected override def startupAction: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.existingFic(record), getButtonsForExisting(record))).unit

  override def onMessage(msg: Message): UIO[Scenario] = StartScenario().onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] =
    query.data match
      case Buttons.addToBacklog.callbackData      => patchStats(record.details.copy(backlog = true), query)
      case Buttons.removeFromBacklog.callbackData => patchStats(record.details.copy(backlog = false), query)

      case Buttons.markAsRead.callbackData          => patchStats(record.details.copy(read = true), query)
      case Buttons.markAsStartedToday.callbackData  => patchDates(addStartDate(_, LocalDate.now().toString))(query)
      case Buttons.markAsFinishedToday.callbackData => patchDates(addFinishDate(_, LocalDate.now().toString))(query)
      case Buttons.cancelStartedToday.callbackData  => patchDates(cancelStartedToday)(query)
      case Buttons.cancelFinishedToday.callbackData => patchDates(cancelFinishedToday)(query)

      case Buttons.rateNever.callbackData     => patchStats(record.details.copy(quality = Some(Quality.Never)), query)
      case Buttons.rateMeh.callbackData       => patchStats(record.details.copy(quality = Some(Quality.Meh)), query)
      case Buttons.rateOk.callbackData        => patchStats(record.details.copy(quality = Some(Quality.Ok)), query)
      case Buttons.rateNice.callbackData      => patchStats(record.details.copy(quality = Some(Quality.Nice)), query)
      case Buttons.rateBrilliant.callbackData => patchStats(record.details.copy(quality = Some(Quality.Brilliant)), query)

      case Buttons.rateFire.callbackData    => patchStats(record.details.copy(fire = true), query)
      case Buttons.rateNotFire.callbackData => patchStats(record.details.copy(fire = false), query)

      case Buttons.addComment.callbackData => bot.answerCallbackQuery(query).flatMap(_ => CommentScenario(record).withStartup)

      case Buttons.sendToKindle.callbackData =>
        record.fic.ficType match
          case FicType.Work => bot.answerCallbackQuery(query).flatMap(_ => SendToKindleScenario(record).withStartup)
          case FicType.Series =>
            bot
              .answerCallbackQuery(query, text = Some("Sorry, can't send series to Kindle yet, please send each work separately"))
              .map(_ => this)

      case _ => unknownCallbackQuery(query).map(_ => this)

  private def patchStats(newStats: FicDetails, query: CallbackQuery) =
    patchFic(s"patching fic with id ${record.key}")(patchFicStats(_, newStats))(query)

  private def patchDates(patch: UserFicRecord => IO[Throwable, UserFicRecord])(query: CallbackQuery) =
    patchFic(s"updating fic ${record.key} read dates")(patch)(query)

  private def patchFic(actionName: String)(patch: UserFicRecord => IO[Throwable, UserFicRecord])(query: CallbackQuery) =
    query.message
      .collect { case msg: Message =>
        (for {
          patchedFic <- patch(record)
          msgData <- ZIO.succeed(
            MessageData(MessageText.existingFic(patchedFic), getButtonsForExisting(patchedFic))
          )
          _ <- bot.answerCallbackQuery(query)
          _ <- bot.editMessage(msg, msgData)
        } yield ExistingFicScenario(patchedFic)) |> sendOnError(actionName)
      }
      .getOrElse(ZIO.succeed(this))
