package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.Work
import com.kapibarabanka.kapibarabot.main.BotError.*
import com.kapibarabanka.kapibarabot.main.Buttons.*
import com.kapibarabanka.kapibarabot.domain.{MyFicRecord, MyFicStats, Quality}
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, Buttons, MessageData, MessageText, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

case class ExistingFicScenario(record: MyFicRecord)(implicit
    bot: BotApiWrapper,
    airtable: AirtableClient,
    ao3: Ao3
) extends Scenario,
      WithErrorHandling(bot):

  protected override def startupAction: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.existingFic(record), getButtonsForExisting(record.stats))).unit

  override def onMessage(msg: Message): UIO[Scenario] = StartScenario().onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = {
    query.data match
      case Buttons.addToBacklog.callbackData      => patchStats(record.stats.copy(backlog = true), query)
      case Buttons.removeFromBacklog.callbackData => patchStats(record.stats.copy(backlog = false), query)
      case Buttons.markAsRead.callbackData        => patchStats(record.stats.copy(read = true), query)
      case Buttons.kindleToDo.callbackData        => patchStats(record.stats.copy(kindleToDo = true), query)
      case Buttons.markAsReadToday.callbackData   => patchStats(record.stats.withReadToday, query)

      case Buttons.rateNever.callbackData     => patchStats(record.stats.copy(quality = Some(Quality.Never)), query)
      case Buttons.rateMeh.callbackData       => patchStats(record.stats.copy(quality = Some(Quality.Meh)), query)
      case Buttons.rateOk.callbackData        => patchStats(record.stats.copy(quality = Some(Quality.Ok)), query)
      case Buttons.rateNice.callbackData      => patchStats(record.stats.copy(quality = Some(Quality.Nice)), query)
      case Buttons.rateBrilliant.callbackData => patchStats(record.stats.copy(quality = Some(Quality.Brilliant)), query)

      case Buttons.addComment.callbackData => bot.answerCallbackQuery(query).flatMap(_ => CommentScenario(record).withStartup)

      case Buttons.sendToKindle.callbackData =>
        record.fic match
          case w: Work => bot.answerCallbackQuery(query).flatMap(_ => SendToKindleScenario(record).withStartup)
          case _ =>
            patchStats(
              record.stats.copy(kindleToDo = true),
              query,
              Some("Can't convert series to EPUB, marked as Kindle TODO")
            )

      case _ => unknownCallbackQuery(query).map(_ => this)
  }

  private def patchStats(newStats: MyFicStats, query: CallbackQuery, callbackResponse: Option[String] = None) = {
    query.message
      .collect { case msg: Message =>
        (for {
          patchedRecord <- airtable.patchFicStats(record.id.get, newStats)
          msgData <- ZIO.succeed(MessageData(MessageText.existingFic(patchedRecord), getButtonsForExisting(patchedRecord.stats)))
          _       <- bot.answerCallbackQuery(query, text = callbackResponse)
          _       <- bot.editMessage(msg, msgData)
        } yield ExistingFicScenario(patchedRecord)) |> sendOnError(s"patching record with id ${record.id.get}")
      }
      .getOrElse(ZIO.succeed(this))
  }
