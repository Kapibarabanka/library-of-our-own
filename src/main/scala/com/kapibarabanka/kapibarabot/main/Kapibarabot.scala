package com.kapibarabanka.kapibarabot.main

import cats.Parallel
import cats.effect.Async
import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.main.scenarios.{Scenario, StartScenario}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.Config.allowedChats
import telegramium.bots.*
import telegramium.bots.high.{Api, LongPollBot}
import zio.*
import telegramium.bots.high.implicits.*

import scala.collection.mutable
import scala.collection.mutable.*

class Kapibarabot()(implicit
    bot: Api[Task],
    asyncF: Async[Task],
    parallel: Parallel[Task],
    airtable: AirtableClient,
    ao3: Ao3
) extends LongPollBot[Task](bot):
  val scenarios: mutable.Map[String, Scenario] = mutable.Map.empty[String, Scenario]

  override def start(): Task[Unit] = for {
    _ <- ZIO.collectAll(allowedChats map setup)
    _ <- super.start()
  } yield ()

  private def setup(chaId: String) = {
    implicit val botApiWrapper: BotApiWrapper = new BotApiWrapper(chaId)
    implicit val db: FanficDbOld              = FanficDbOld()
    scenarios.addOne((chaId, StartScenario()))
    db.init
  }

  override def onMessage(msg: Message): Task[Unit] =
    useScenario(msg.chat.id.toString)(scenario => scenario.onMessage(msg))

  override def onCallbackQuery(query: CallbackQuery): Task[Unit] =
    useScenario(query.from.id.toString)(scenario => scenario.onCallbackQuery(query))

  private def useScenario(chatId: String)(f: Scenario => Task[Scenario]): Task[Unit] =
    scenarios.get(chatId) match
      case None => sendMessage(chatId = ChatStrId(chatId), text = "You are not in the allowed user list").exec.unit
      case Some(scenario) =>
        for {
          newScenario <- f(scenario)
          _ <- ZIO.succeed({
            scenarios(chatId) = newScenario
          })
        } yield ()
