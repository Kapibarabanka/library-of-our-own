package com.kapibarabanka.kapibarabot.bot.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.domain.{MyFicRecord, MyFicStats}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import telegramium.bots.high.Api
import telegramium.bots.{CallbackQuery, Message}
import zio.{Task, ZIO}
import java.time.LocalDate

case class CommentScenario(record: MyFicRecord)(implicit
    bot: Api[Task],
    airtable: AirtableClient,
    ao3: Ao3
) extends Scenario:
  protected override def startupAction: Task[Unit] = sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): Task[Scenario] = for {
    newComment <- ZIO.succeed(
      record.stats.commentOption.getOrElse("") + s"${LocalDate.now()}:\n${msg.text.getOrElse("NO_TEXT")}\n"
    )
    logPatching   <- sendText("Patching record...")
    patchedRecord <- airtable.patchFicStats(record.id.get, MyFicStats(commentOption = Some(newComment)))
    _             <- editLogText(logPatching, "Successfully patched record! Here it is:")
    nextScenario  <- ExistingFicScenario(patchedRecord).withStartup
  } yield nextScenario

  override def onCallbackQuery(query: CallbackQuery): Task[Scenario] = StartScenario().onCallbackQuery(query)
