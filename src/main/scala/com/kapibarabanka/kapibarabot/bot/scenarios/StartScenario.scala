package com.kapibarabanka.kapibarabot.bot.scenarios

import com.kapibarabanka.ao3scrapper.{Ao3, Ao3Url}
import com.kapibarabanka.kapibarabot.bot.Constants.myChatId
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import telegramium.bots.high.Api
import telegramium.bots.high.Methods.sendMessage
import telegramium.bots.high.implicits.*
import telegramium.bots.{CallbackQuery, ChatIntId, Message}
import zio.*

case class StartScenario()(implicit bot: Api[Task], airtable: AirtableClient, ao3: Ao3) extends Scenario:
  protected override def startupAction: Task[Unit] = ZIO.unit

  override def onMessage(msg: Message): Task[Scenario] = {
    if (myChatId != ChatIntId(msg.chat.id))
      sendMessage(chatId = ChatIntId(msg.chat.id), text = "Only this bot's creator can use it").exec.unit.map(_ => this)
    else
      for {
        scenarioOption <- tryParseFicLink(msg.text.getOrElse("NO_TEXT"))
        nextScenario <- scenarioOption match
          case Some(value) => ZIO.succeed(value)
          case None        => sendText("Not AO3 link, don't know what to do :c").map(_ => this)
      } yield nextScenario
  }

  override def onCallbackQuery(query: CallbackQuery): Task[Scenario] = unknownCallbackQuery(query).map(_ => this)

  private def tryParseFicLink(text: String): Task[Option[Scenario]] = Ao3Url.tryParseFicId(text) match
    case None => ZIO.succeed(None)
    case Some((_, id)) =>
      for {
        recordOption <- airtable.getFicByLink(text)
        nextScenario <- recordOption match
          case Some(record) => ExistingFicScenario(record).withStartup
          case None         => NewFicScenario(text).withStartup

      } yield Some(nextScenario)
