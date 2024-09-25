package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.ao3scrapper.Ao3
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.{FicComment, UserFicRecord, UserFicKey, FicDetails}
import com.kapibarabanka.kapibarabot.main.{BotApiWrapper, WithErrorHandling}
import com.kapibarabanka.kapibarabot.airtable.AirtableClient
import com.kapibarabanka.kapibarabot.sqlite.FanficDbOld
import com.kapibarabanka.kapibarabot.utils.Config.myChatId
import telegramium.bots.{CallbackQuery, Message}
import zio.*

trait Scenario(implicit bot: BotApiWrapper, airtable: AirtableClient, ao3: Ao3, db: FanficDbOld) extends WithErrorHandling:
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

  protected def patchFicStats(record: UserFicRecord, stats: FicDetails): IO[Throwable, UserFicRecord] =
    executeAndUpdateAirtable(db.patchFicStats(record, stats))

  protected def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord] =
    executeAndUpdateAirtable(db.addComment(record, comment))

  protected def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord] =
    executeAndUpdateAirtable(db.addStartDate(record, startDate))

  protected def addFinishDate(record: UserFicRecord, finishDate: String): IO[Throwable, UserFicRecord] =
    executeAndUpdateAirtable(db.addFinishDate(record, finishDate))

  protected def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] =
    executeAndUpdateAirtable(db.cancelStartedToday(record))

  protected def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] =
    executeAndUpdateAirtable(db.cancelFinishedToday(record))

  private def executeAndUpdateAirtable(action: IO[Throwable, UserFicRecord]) = for {
    fic <- action
    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic) else ZIO.unit
  } yield fic
