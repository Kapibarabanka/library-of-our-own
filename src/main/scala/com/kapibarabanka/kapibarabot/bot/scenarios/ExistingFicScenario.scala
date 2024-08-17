package com.kapibarabanka.kapibarabot.bot.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.Work
import com.kapibarabanka.kapibarabot.bot.*
import com.kapibarabanka.kapibarabot.bot.BotError.*
import com.kapibarabanka.kapibarabot.bot.Buttons.*
import com.kapibarabanka.kapibarabot.domain.{MyFicRecord, MyFicStats, Quality}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import telegramium.bots.*
import telegramium.bots.high.Api
import telegramium.bots.high.Methods.answerCallbackQuery
import telegramium.bots.high.implicits.*
import zio.*
import scalaz.Scalaz.ToIdOps

case class ExistingFicScenario(record: MyFicRecord)(implicit
    bot: Api[Task],
    airtable: AirtableClient,
    ao3: Ao3
) extends Scenario:

  protected override def startupAction: UIO[Unit] =
    sendMessageData(MessageData(MessageText.existingFic(record), getButtonsForExisting(record.stats))).unit

  override def onMessage(msg: Message): UIO[Scenario] = StartScenario().onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = {
    def patch(stats: MyFicStats) = airtable.patchFicStats(_: String, stats)

    def markAsReadToday = airtable.markAsReadToday(_: String)

    query.data match
      case Buttons.addToBacklog.callbackData      => onPatchStats(patch(MyFicStats(backlogOption = Some(true))), query)
      case Buttons.removeFromBacklog.callbackData => onPatchStats(patch(MyFicStats(backlogOption = Some(false))), query)
      case Buttons.markAsRead.callbackData        => onPatchStats(patch(MyFicStats(readOption = Some(true))), query)
      case Buttons.kindleToDo.callbackData        => onPatchStats(patch(MyFicStats(kindleToDoOption = Some(true))), query)
      case Buttons.markAsReadToday.callbackData   => onPatchStats(markAsReadToday, query)

      case Buttons.rateNever.callbackData     => onPatchStats(patch(MyFicStats(qualityOption = Some(Quality.Never))), query)
      case Buttons.rateMeh.callbackData       => onPatchStats(patch(MyFicStats(qualityOption = Some(Quality.Meh))), query)
      case Buttons.rateOk.callbackData        => onPatchStats(patch(MyFicStats(qualityOption = Some(Quality.Ok))), query)
      case Buttons.rateNice.callbackData      => onPatchStats(patch(MyFicStats(qualityOption = Some(Quality.Nice))), query)
      case Buttons.rateBrilliant.callbackData => onPatchStats(patch(MyFicStats(qualityOption = Some(Quality.Brilliant))), query)

      case Buttons.addComment.callbackData =>
        answerCallbackQuery(callbackQueryId = query.id).exec.unit.flatMap(_ =>
          CommentScenario(record).withStartup
        ) |> tryAndSendOnError()

      case Buttons.sendToKindle.callbackData =>
        record.fic match
          case w: Work =>
            answerCallbackQuery(callbackQueryId = query.id).exec.unit.flatMap(_ =>
              SendToKindleScenario(record).withStartup
            ) |> tryAndSendOnError()
          case _ =>
            onPatchStats(
              patch(MyFicStats(kindleToDoOption = Some(true))),
              query,
              Some("Can't convert fic to EPUB, marked as Kindle TODO")
            )

      case _ => unknownCallbackQuery(query).map(_ => this)
  }

  private def onPatchStats(patchStats: String => Task[MyFicRecord], query: CallbackQuery, response: Option[String] = None) = {
    query.message
      .collect { case msg: Message =>
        (for {
          patchedRecord <- patchStats(record.id.get)
          msgData <- ZIO.succeed(MessageData(MessageText.existingFic(patchedRecord), getButtonsForExisting(patchedRecord.stats)))
          _       <- answerCallbackQuery(callbackQueryId = query.id, text = response).exec
          _       <- editMessage(msg, msgData)
        } yield this) |> tryAndSendOnError()
      }
      .getOrElse(ZIO.succeed(this))
  }
