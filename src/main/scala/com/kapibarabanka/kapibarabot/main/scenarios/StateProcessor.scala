package com.kapibarabanka.kapibarabot.main.scenarios

import com.kapibarabanka.kapibarabot.utils.BotWithChatId
import telegramium.bots.{CallbackQuery, Message}
import zio.*

trait StateProcessor(currentState: BotState, bot: BotWithChatId) extends WithErrorHandling:
  def startup: UIO[Unit]
  def onMessage(msg: Message): UIO[BotState]
  def onCallbackQuery(query: CallbackQuery): UIO[BotState]

  def sendOnError(actionName: String)(action: IO[Throwable, BotState]): UIO[BotState] =
    sendOnError(StartBotState())(actionName)(action)

  def sendOnErrors(errorToMessage: PartialFunction[Throwable, String])(action: IO[Throwable, BotState]): UIO[BotState] =
    sendOnErrors(StartBotState())(errorToMessage)(action)

  protected def unknownCallbackQuery(query: CallbackQuery): ZIO[Any, Nothing, Unit] =
    bot.answerCallbackQuery(
      query,
      Some(s"You chose ${query.data} and I don't know what to do with it")
    )

//  protected def patchFicStats(record: UserFicRecord, stats: FicDetails): IO[Throwable, UserFicRecord] =
//    executeAndUpdateAirtable(db.patchFicStats(record, stats))
//
//  protected def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord] =
//    executeAndUpdateAirtable(db.addComment(record, comment))
//
//  protected def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord] =
//    executeAndUpdateAirtable(db.addStartDate(record, startDate))
//
//  protected def addFinishDate(record: UserFicRecord, finishDate: String): IO[Throwable, UserFicRecord] =
//    executeAndUpdateAirtable(db.addFinishDate(record, finishDate))
//
//  protected def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] =
//    executeAndUpdateAirtable(db.cancelStartedToday(record))
//
//  protected def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] =
//    executeAndUpdateAirtable(db.cancelFinishedToday(record))
//
//  private def executeAndUpdateAirtable(action: IO[Throwable, UserFicRecord]) = for {
//    fic <- action
//    _   <- if (bot.chatId == myChatId) airtable.upsertFic(fic) else ZIO.unit
//  } yield fic
