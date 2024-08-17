package com.kapibarabanka.kapibarabot.bot

import cats.Parallel
import cats.effect.Async
import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.kapibarabot.bot.scenarios.{Scenario, StartScenario}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import telegramium.bots.*
import telegramium.bots.high.{Api, LongPollBot}
import zio.*

class Kapibarabot()(implicit
    bot: Api[Task],
    asyncF: Async[Task],
    parallel: Parallel[Task],
    airtable: AirtableClient,
    ao3: Ao3
) extends LongPollBot[Task](bot):

  var scenario: Scenario = StartScenario()

  override def onMessage(msg: Message): Task[Unit] =
    for {
      newScenario <- scenario.onMessage(msg)
      _ <- ZIO.succeed({
        scenario = newScenario
      })
    } yield ()

  override def onCallbackQuery(query: CallbackQuery): Task[Unit] =
    for {
      newScenario <- scenario.onCallbackQuery(query)
      _ <- ZIO.succeed({
        scenario = newScenario
      })
    } yield ()
