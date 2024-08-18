package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Url}
import com.kapibarabanka.kapibarabot.main.Constants.myChatId
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, ChatIntId, Message}
import zio.*

case class StartScenario()(implicit bot: BotApiWrapper, airtable: AirtableClient, ao3: Ao3)
    extends Scenario,
      WithErrorHandling(bot):
  protected override def startupAction: UIO[Unit] = ZIO.unit

  override def onMessage(msg: Message): UIO[Scenario] = {
    if (myChatId != ChatIntId(msg.chat.id))
      bot.wrongChatError(msg).map(_ => this)
    else
      for {
        scenarioOption <- tryParseFicLink(msg.text.getOrElse("NO_TEXT"))
        nextScenario <- scenarioOption match
          case Some(value) => ZIO.succeed(value)
          case None        => bot.sendText("Not AO3 link, don't know what to do :c").map(_ => this)
      } yield nextScenario
  }

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = unknownCallbackQuery(query).map(_ => this)

  private def tryParseFicLink(text: String): UIO[Option[Scenario]] = Ao3Url.tryParseFicId(text) match
    case None => ZIO.succeed(None)
    case Some((_, id)) =>
      (for {
        recordOption <- airtable.getFicByLink(text)
        nextScenario <- recordOption match
          case Some(record) => ExistingFicScenario(record).withStartup
          case None         => NewFicScenario(text).withStartup

      } yield nextScenario) |> sendOnError("looking for fic in Airtable") map (scenario => Some(scenario))
