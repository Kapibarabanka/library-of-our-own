package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDetails, UserFicKey, UserFicRecord}
import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.repos.FicDetailsRepo
import zio.{IO, ZIO, ZLayer}

trait FicDetailsService:
  def getUserEmail(userId: String): IO[SqliteError, Option[String]]
  def getUserBacklog(userId: String): IO[SqliteError, List[UserFicRecord]]
  def getOrCreateUserFic(key: UserFicKey): IO[SqliteError, UserFicRecord]
  def patchFicDetails(record: UserFicRecord, details: FicDetails): IO[SqliteError, UserFicRecord]
  def addComment(record: UserFicRecord, comment: FicComment): IO[SqliteError, UserFicRecord]
  def addStartDate(record: UserFicRecord, startDate: String): IO[SqliteError, UserFicRecord]
  def addFinishDate(record: UserFicRecord, finishDate: String): IO[SqliteError, UserFicRecord]
  def cancelStartedToday(record: UserFicRecord): IO[SqliteError, UserFicRecord]
  def cancelFinishedToday(record: UserFicRecord): IO[SqliteError, UserFicRecord]

case class FicDetailsServiceImpl(db: KapibarabotDb, ficService: FicService) extends FicDetailsService:
  private val repo = FicDetailsRepo(db)

  override def getUserEmail(userId: String): IO[SqliteError, Option[String]] = repo.getUserEmail(userId)

  override def getUserBacklog(userId: String): IO[SqliteError, List[UserFicRecord]] = for {
    keys    <- repo.getUserBacklog(userId)
    records <- ZIO.collectAll(keys.map(getOrCreateUserFic))
  } yield records

  override def getOrCreateUserFic(key: UserFicKey): IO[SqliteError, UserFicRecord] = for {
    details       <- repo.getOrCreateDetails(key)
    readDatesInfo <- repo.getReadDatesInfo(key)
    comments      <- repo.getAllComments(key)
    fic           <- ficService.getFicOption(key.ficId, key.ficType).map(_.get)
  } yield UserFicRecord(
    userId = key.userId,
    fic = fic,
    readDatesInfo = readDatesInfo,
    comments = comments,
    details = details
  )

  override def patchFicDetails(record: UserFicRecord, details: FicDetails): IO[SqliteError, UserFicRecord] = for {
    _ <- repo.patchDetails(record.key, details)
  } yield record.copy(details = details)

  override def addComment(record: UserFicRecord, comment: FicComment): IO[SqliteError, UserFicRecord] = for {
    _        <- repo.addComment(record.key, comment)
    comments <- repo.getAllComments(record.key)
  } yield record.copy(comments = comments)

  override def addStartDate(record: UserFicRecord, startDate: String): IO[SqliteError, UserFicRecord] = for {
    _     <- repo.addStartDate(record.key, startDate)
    dates <- repo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def addFinishDate(record: UserFicRecord, finishDate: String): IO[SqliteError, UserFicRecord] = for {
    _          <- repo.addFinishDate(record.key, finishDate)
    dates      <- repo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def cancelStartedToday(record: UserFicRecord): IO[SqliteError, UserFicRecord] = for {
    _     <- repo.cancelStartedToday(record.key)
    dates <- repo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def cancelFinishedToday(record: UserFicRecord): IO[SqliteError, UserFicRecord] = for {
    _     <- repo.cancelFinishedToday(record.key)
    dates <- repo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

object FicDetailsServiceImpl:
  val layer: ZLayer[FicService & KapibarabotDb, Nothing, FicDetailsServiceImpl] = ZLayer {
    for {
      ficService <- ZIO.service[FicService]
      db         <- ZIO.service[KapibarabotDb]
    } yield FicDetailsServiceImpl(db, ficService)
  }

object FicDetailsService:
  def getOrCreateUserFic(key: UserFicKey): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.getOrCreateUserFic(key))

  def patchFicStats(record: UserFicRecord, details: FicDetails): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.patchFicDetails(record, details))

  def addComment(record: UserFicRecord, comment: FicComment): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.addComment(record, comment))

  def addStartDate(record: UserFicRecord, startDate: String): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.addStartDate(record, startDate))

  def addFinishDate(record: UserFicRecord, finishDate: String): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.addFinishDate(record, finishDate))

  def cancelStartedToday(record: UserFicRecord): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.cancelStartedToday(record))

  def cancelFinishedToday(record: UserFicRecord): ZIO[FicDetailsService, SqliteError, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.cancelFinishedToday(record))
