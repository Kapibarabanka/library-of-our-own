package kapibarabanka.lo3.api
package sqlite.services

import sqlite.SqliteError
import sqlite.repos.*

import kapibarabanka.lo3.models.tg.*
import zio.{IO, ZIO}

case class FicDetailsService(db: KapibarabotDb, ficService: FicService):
  private val detailsRepo  = FicDetailsRepo(db)
  private val datesRepo    = ReadDatesRepo(db)
  private val commentsRepo = CommentsRepo(db)

  def getUserBacklog(userId: String): IO[SqliteError, List[UserFicRecord]] = for {
    keys    <- detailsRepo.getUserBacklog(userId)
    records <- ZIO.collectAll(keys.map(getOrCreateUserFic))
  } yield records

  def getOrCreateUserFic(key: UserFicKey): IO[SqliteError, UserFicRecord] = for {
    details       <- detailsRepo.getOrCreateDetails(key)
    readDatesInfo <- datesRepo.getReadDatesInfo(key)
    comments      <- commentsRepo.getAllComments(key)
    fic           <- ficService.getByIdOption(key.ficId, key.ficType).map(_.get)
  } yield UserFicRecord(
    userId = key.userId,
    fic = fic,
    readDatesInfo = readDatesInfo,
    comments = comments,
    details = details
  )

  def patchFicDetails(record: UserFicRecord, details: FicDetails): IO[SqliteError, UserFicRecord] = for {
    _ <- detailsRepo.patchDetails(record.key, details)
  } yield record.copy(details = details)

  def addComment(record: UserFicRecord, comment: FicComment): IO[SqliteError, UserFicRecord] = for {
    _        <- commentsRepo.addComment(record.key, comment)
    comments <- commentsRepo.getAllComments(record.key)
  } yield record.copy(comments = comments)

  def addStartDate(record: UserFicRecord, startDate: String): IO[SqliteError, UserFicRecord] = for {
    _     <- datesRepo.addStartDate(record.key, startDate)
    dates <- datesRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  def addFinishDate(record: UserFicRecord, finishDate: String): IO[SqliteError, UserFicRecord] = for {
    _          <- datesRepo.addFinishDate(record.key, finishDate)
    dates      <- datesRepo.getReadDatesInfo(record.key)
    newDetails <- ZIO.succeed(record.details.copy(backlog = false))
    _          <- detailsRepo.patchDetails(record.key, newDetails)
  } yield record.copy(readDatesInfo = dates, details = newDetails)

  def cancelStartedToday(record: UserFicRecord): IO[SqliteError, UserFicRecord] = for {
    _     <- datesRepo.cancelStartedToday(record.key)
    dates <- datesRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  def cancelFinishedToday(record: UserFicRecord): IO[SqliteError, UserFicRecord] = for {
    _     <- datesRepo.cancelFinishedToday(record.key)
    dates <- datesRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)
