package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.domain.MyFicRecord
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

import java.time.LocalDate

case class CommentScenario(record: MyFicRecord)(implicit
    bot: BotApiWrapper,
    airtable: AirtableClient,
    ao3: Ao3
) extends Scenario,
      WithErrorHandling(bot):
  protected override def startupAction: UIO[Unit] = bot.sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): UIO[Scenario] =
    (for {
      newComment <- ZIO.succeed(
        record.stats.comment.getOrElse("") + s"${LocalDate.now()}:\n${msg.text.getOrElse("NO_TEXT")}\n"
      )
      logPatching   <- bot.sendText("Patching record...")
      patchedRecord <- airtable.patchFicStats(record.id.get, record.stats.copy(comment = Some(newComment)))
      _             <- bot.editLogText(logPatching, "Successfully patched record! Here it is:")
      nextScenario  <- ExistingFicScenario(patchedRecord).withStartup
    } yield nextScenario) |> sendOnError(s"patching record with id ${record.id}")

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = StartScenario().onCallbackQuery(query)
