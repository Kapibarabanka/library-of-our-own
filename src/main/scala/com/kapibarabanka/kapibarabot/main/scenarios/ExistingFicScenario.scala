package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.FicType
import com.kapibarabanka.kapibarabot.domain.{FicDisplayModel, MyFicStats, Quality}
import com.kapibarabanka.kapibarabot.main.BotError.*
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, MessageData, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDb
import com.kapibarabanka.kapibarabot.utils.Buttons.*
import com.kapibarabanka.kapibarabot.utils.{Buttons, MessageText}
import scalaz.Scalaz.ToIdOps
import telegramium.bots.*
import zio.*

case class ExistingFicScenario(fic: FicDisplayModel)(implicit
    bot: BotApiWrapper,
    airtable: AirtableClient,
    ao3: Ao3,
    db: FanficDb
) extends Scenario,
      WithErrorHandling(bot):

  protected override def startupAction: UIO[Unit] =
    bot.sendMessage(MessageData(MessageText.existingFic(fic), getButtonsForExisting(fic.stats))).unit

  override def onMessage(msg: Message): UIO[Scenario] = StartScenario().onMessage(msg)

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = {
    query.data match
      case Buttons.addToBacklog.callbackData      => patchStats(fic.stats.copy(backlog = true), query)
      case Buttons.removeFromBacklog.callbackData => patchStats(fic.stats.copy(backlog = false), query)
      case Buttons.markAsRead.callbackData        => patchStats(fic.stats.copy(read = true), query)
      case Buttons.markAsReadToday.callbackData   => patchStats(fic.stats.withReadToday, query)

      case Buttons.rateNever.callbackData     => patchStats(fic.stats.copy(quality = Some(Quality.Never)), query)
      case Buttons.rateMeh.callbackData       => patchStats(fic.stats.copy(quality = Some(Quality.Meh)), query)
      case Buttons.rateOk.callbackData        => patchStats(fic.stats.copy(quality = Some(Quality.Ok)), query)
      case Buttons.rateNice.callbackData      => patchStats(fic.stats.copy(quality = Some(Quality.Nice)), query)
      case Buttons.rateBrilliant.callbackData => patchStats(fic.stats.copy(quality = Some(Quality.Brilliant)), query)

      case Buttons.rateFire.callbackData    => patchStats(fic.stats.copy(fire = true), query)
      case Buttons.rateNotFire.callbackData => patchStats(fic.stats.copy(fire = false), query)

      case Buttons.addComment.callbackData => bot.answerCallbackQuery(query).flatMap(_ => CommentScenario(fic).withStartup)

      case Buttons.sendToKindle.callbackData =>
        fic.ficType match
          case FicType.Work => bot.answerCallbackQuery(query).flatMap(_ => SendToKindleScenario(fic).withStartup)
          case FicType.Series =>
            bot
              .answerCallbackQuery(query, text = Some("Sorry, can't send series to Kindle yet, please send each work separately"))
              .map(_ => this)

      case _ => unknownCallbackQuery(query).map(_ => this)
  }

  private def patchStats(newStats: MyFicStats, query: CallbackQuery) = {
    query.message
      .collect { case msg: Message =>
        (for {
          patchedFic <- patchFicStats(fic.id, fic.ficType, newStats)
          msgData <- ZIO.succeed(
            MessageData(MessageText.existingFic(patchedFic), getButtonsForExisting(patchedFic.stats))
          )
          _ <- bot.answerCallbackQuery(query)
          _ <- bot.editMessage(msg, msgData)
        } yield ExistingFicScenario(patchedFic)) |> sendOnError(s"patching fic with id ${fic.id}")
      }
      .getOrElse(ZIO.succeed(this))
  }
