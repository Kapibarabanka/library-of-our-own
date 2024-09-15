package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Url}
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDb
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

case class StartScenario()(implicit bot: BotApiWrapper, airtable: AirtableClient, ao3: Ao3, db: FanficDb)
    extends Scenario,
      WithErrorHandling(bot):
  protected override def startupAction: UIO[Unit] = ZIO.unit

  override def onMessage(msg: Message): UIO[Scenario] = for {
    scenarioOption <- tryParseFicLink(msg.text.getOrElse("NO_TEXT"))
    nextScenario <- scenarioOption match
      case Some(value) => ZIO.succeed(value)
      case None        => bot.sendText("Not AO3 link, don't know what to do :c " + msg.text).map(_ => this)
  } yield nextScenario

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = unknownCallbackQuery(query).map(_ => this)

  private def tryParseFicLink(text: String): UIO[Option[Scenario]] = Ao3Url.tryParseFicId(text) match
    case None => ZIO.succeed(None)
    case Some((_, id)) =>
      (for {
        maybeFic <- db.fics.getFic(id)
        nextScenario <- maybeFic match
          case Some(record) => ExistingFicScenario(record).withStartup
          case None         => NewFicScenario(text).withStartup

      } yield nextScenario) |> sendOnError("looking for fic in Airtable") map (scenario => Some(scenario))
