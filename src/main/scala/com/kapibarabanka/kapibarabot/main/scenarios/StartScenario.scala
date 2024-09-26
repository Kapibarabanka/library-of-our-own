package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Url}
import com.kapibarabanka.kapibarabot.domain.UserFicKey
import com.kapibarabanka.kapibarabot.airtable.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.BotWithChatId
import scalaz.Scalaz.ToIdOps
import telegramium.bots.{CallbackQuery, Message}
import zio.*

case class StartScenario()(implicit bot: BotWithChatId, airtable: AirtableClient, ao3: Ao3, db: FanficDbOld)
    extends Scenario,
      WithErrorHandling(bot):
  protected override def startupAction: UIO[Unit] = ZIO.unit

  override def onMessage(msg: Message): UIO[Scenario] = for {
    scenarioOption <- tryParseFicLink(msg)
    nextScenario <- scenarioOption match
      case Some(value) => ZIO.succeed(value)
      case None        => bot.sendText("Not AO3 link, don't know what to do :c " + msg.text).map(_ => this)
  } yield nextScenario

  override def onCallbackQuery(query: CallbackQuery): UIO[Scenario] = unknownCallbackQuery(query).map(_ => this)

  private def tryParseFicLink(msg: Message): UIO[Option[Scenario]] =
    val text = msg.text.getOrElse("NO_TEXT")
    Ao3Url.tryParseFicId(text) match
      case None => ZIO.succeed(None)
      case Some((ficType, ficId)) =>
        (for {
          ficExists <- db.ficIsInDb(ficId, ficType)
          nextScenario <-
            if (!ficExists)
              NewFicScenario(text).withStartup
            else
              for {
                record <- db.getOrCreateUserFic(UserFicKey(bot.chatId, ficId, ficType))
                next   <- ExistingFicScenario(record).withStartup
              } yield next
        } yield nextScenario) |> sendOnError("looking for fic in Airtable") map (scenario => Some(scenario))
