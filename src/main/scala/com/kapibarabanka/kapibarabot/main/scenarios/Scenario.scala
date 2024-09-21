package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel, MyFicStats}
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, WithErrorHandling}
import com.kapibarabanka.kapibarabot.persistence.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDb
import com.kapibarabanka.kapibarabot.utils.Constants.myChatId
import telegramium.bots.{CallbackQuery, Message}
import zio.*

trait Scenario(implicit bot: BotApiWrapper, airtable: AirtableClient, ao3: Ao3, db: FanficDb) extends WithErrorHandling:
  protected def startupAction: UIO[Unit]
  def onMessage(msg: Message): UIO[Scenario]
  def onCallbackQuery(query: CallbackQuery): UIO[Scenario]

  def withStartup: UIO[Scenario] = startupAction.map(_ => this)

  def sendOnError(actionName: String)(action: ZIO[Any, Throwable, Scenario]): UIO[Scenario] =
    sendOnError(StartScenario())(actionName)(action)

  def sendOnErrors(errorToMessage: PartialFunction[Throwable, String])(action: ZIO[Any, Throwable, Scenario]): UIO[Scenario] =
    sendOnErrors(StartScenario())(errorToMessage)(action)

  protected def unknownCallbackQuery(query: CallbackQuery): ZIO[Any, Nothing, Unit] =
    bot.answerCallbackQuery(
      query,
      text = Some(s"You chose ${query.data} and I don't know what to do with it")
    )

  protected def addWork(work: Work): IO[Throwable, FicDisplayModel] = for {
    fic <- db.add(work)
    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic) else ZIO.unit
  } yield fic

  protected def addSeries(series: Series): IO[Throwable, FicDisplayModel] = for {
    fic <- db.add(series)
    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic) else ZIO.unit
  } yield fic

  protected def patchFicStats(ficId: String, ficType: FicType, stats: MyFicStats): IO[Throwable, FicDisplayModel] = for {
    fic <- db.patchFicStats(ficId, ficType, stats)
    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic) else ZIO.unit
  } yield fic

  protected def addComment(ficId: String, ficType: FicType, comment: FicComment): IO[Throwable, FicDisplayModel] = for {
    _   <- db.addComment(ficId, ficType, comment)
    fic <- db.getFicOption(ficId, ficType)
    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic.get) else ZIO.unit
  } yield fic.get
