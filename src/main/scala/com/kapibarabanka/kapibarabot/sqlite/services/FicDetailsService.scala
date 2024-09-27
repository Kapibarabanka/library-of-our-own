package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDetails, UserFicKey, UserFicRecord}
import com.kapibarabanka.kapibarabot.sqlite.repos.FicDetailsRepo
import com.kapibarabanka.kapibarabot.sqlite.services.KapibarabotDb
import zio.{IO, ZIO, ZLayer}

trait FicDetailsService:
  def getUserEmail(userId: String): IO[Throwable, Option[String]]
  def getOrCreateUserFic(key: UserFicKey): IO[Throwable, UserFicRecord]
  def patchFicStats(record: UserFicRecord, details: FicDetails): IO[Throwable, UserFicRecord]
  def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord]
  def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord]
  def addFinishDate(record: UserFicRecord, finishDate: String): IO[Throwable, UserFicRecord]
  def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord]
  def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord]

case class FicDetailsServiceImpl(db: KapibarabotDb, ficService: FicService) extends FicDetailsService:
  private val repo = FicDetailsRepo(db)

  override def getUserEmail(userId: String): IO[Throwable, Option[String]] = repo.getUserEmail(userId)

  override def getOrCreateUserFic(key: UserFicKey): IO[Throwable, UserFicRecord] = for {
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

  override def patchFicStats(record: UserFicRecord, details: FicDetails): IO[Throwable, UserFicRecord] = for {
    _ <- repo.patchDetails(record.key, details)
  } yield record.copy(details = details)

  override def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord] = for {
    _        <- repo.addComment(record.key, comment)
    comments <- repo.getAllComments(record.key)
  } yield record.copy(comments = comments)

  override def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord] = for {
    _     <- repo.addStartDate(record.key, startDate)
    dates <- repo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def addFinishDate(record: UserFicRecord, finishDate: String): IO[Throwable, UserFicRecord] = for {
    _          <- repo.addFinishDate(record.key, finishDate)
    details    <- repo.getOrCreateDetails(record.key)
    newDetails <- ZIO.succeed(details.copy(read = true))
    _          <- if (!details.read) patchFicStats(record, newDetails) else ZIO.unit
    dates      <- repo.getReadDatesInfo(record.key)
  } yield record.copy(details = newDetails, readDatesInfo = dates)

  override def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] = for {
    _     <- repo.cancelStartedToday(record.key)
    dates <- repo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] = for {
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
  def getOrCreateUserFic(key: UserFicKey): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.getOrCreateUserFic(key))

  def patchFicStats(record: UserFicRecord, details: FicDetails): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.patchFicStats(record, details))

  def addComment(record: UserFicRecord, comment: FicComment): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.addComment(record, comment))

  def addStartDate(record: UserFicRecord, startDate: String): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.addStartDate(record, startDate))

  def addFinishDate(record: UserFicRecord, finishDate: String): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.addFinishDate(record, finishDate))

  def cancelStartedToday(record: UserFicRecord): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.cancelStartedToday(record))

  def cancelFinishedToday(record: UserFicRecord): ZIO[FicDetailsService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[FicDetailsService](_.cancelFinishedToday(record))
