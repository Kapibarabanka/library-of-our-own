package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.domain.{FicComment, UserFicRecord}
import com.kapibarabanka.kapibarabot.airtable.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

import java.time.LocalDate

case class CommentScenario(record: UserFicRecord)(implicit
    bot: BotWithChatId,
    airtable: AirtableClient,
    ao3: Ao3,
    db: FanficDbOld
) extends Scenario,
      WithErrorHandling(bot):
  protected override def startupAction: UIO[Unit] = bot.sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): UIO[Scenario] =
    (for {
      logPatching    <- bot.sendText("Adding comment...")
      ficWithComment <- addComment(record, FicComment(LocalDate.now().toString, msg.text.getOrElse("")))
      _              <- bot.editLogText(logPatching, "Successfully added comment!")
      nextScenario   <- ExistingFicScenario(ficWithComment).withStartup
    } yield nextScenario) |> sendOnError(s"adding comment to fic ${record.key}")

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = StartScenario().onCallbackQuery(query)
