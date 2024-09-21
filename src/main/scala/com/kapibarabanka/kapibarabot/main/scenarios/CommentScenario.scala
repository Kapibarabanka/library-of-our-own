package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel}
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDb
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

import java.time.LocalDate

case class CommentScenario(fic: FicDisplayModel)(implicit
    bot: BotApiWrapper,
    airtable: AirtableClient,
    ao3: Ao3,
    db: FanficDb
) extends Scenario,
      WithErrorHandling(bot):
  protected override def startupAction: UIO[Unit] = bot.sendText("Send me your thoughts:").unit

  override def onMessage(msg: Message): UIO[Scenario] =
    (for {
      logPatching    <- bot.sendText("Adding comment...")
      ficWithComment <- addComment(fic.id, fic.ficType, FicComment(LocalDate.now().toString, msg.text.getOrElse("")))
      _              <- bot.editLogText(logPatching, "Successfully added comment!")
      nextScenario   <- ExistingFicScenario(ficWithComment).withStartup
    } yield nextScenario) |> sendOnError(s"adding comment to fic ${fic.id}")

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = StartScenario().onCallbackQuery(query)
