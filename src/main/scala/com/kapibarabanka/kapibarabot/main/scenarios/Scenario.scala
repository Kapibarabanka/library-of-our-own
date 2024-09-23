package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel, FicKey, MyFicStats}
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

  protected def addWork(work: Work): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(FicKey.fromWork(work), db.add(work))

  protected def addSeries(series: Series): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(FicKey.fromSeries(series), db.add(series))

  protected def patchFicStats(key: FicKey, stats: MyFicStats): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(key, db.patchFicStats(key, stats))

  protected def addComment(key: FicKey, comment: FicComment): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(key, db.addComment(key, comment))

  protected def addStartDate(key: FicKey, startDate: String): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(key, db.addStartDate(key, startDate))

  protected def addFinishDate(key: FicKey, finishDate: String): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(key, db.addFinishDate(key, finishDate))

  protected def cancelStartedToday(key: FicKey): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(key, db.cancelStartedToday(key))

  protected def cancelFinishedToday(key: FicKey): IO[Throwable, FicDisplayModel] =
    executeAndUpdateAirtable(key, db.cancelFinishedToday(key))

  private def executeAndUpdateAirtable(key: FicKey, action: IO[Throwable, FicDisplayModel]) = for {
    fic <- action
    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic) else ZIO.unit
  } yield fic
