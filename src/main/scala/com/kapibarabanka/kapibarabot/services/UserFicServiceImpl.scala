package com.kapibarabanka.kapibarabot.services

import com.kapibarabanka.kapibarabot.domain.*
import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.sqlite.repos.*
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO, ZLayer}

case class UserFicServiceImpl(db: KapibarabotDb, ficService: FicService) extends UserFicService:
  private val userFicRepo = UserFicRepo()

  override def getOrCreateUserFic(key: UserFicKey): IO[Throwable, UserFicRecord] = for {
    details       <- userFicRepo.getOrCreateDetails(key)
    readDatesInfo <- userFicRepo.getReadDatesInfo(key)
    comments      <- userFicRepo.getAllComments(key)
    fic           <- ficService.getFicOption(key.ficId, key.ficType).map(_.get)
  } yield UserFicRecord(
    userId = key.userId,
    fic = fic,
    readDatesInfo = readDatesInfo,
    comments = comments,
    details = details
  )

  override def patchFicStats(record: UserFicRecord, details: FicDetails): IO[Throwable, UserFicRecord] = for {
    _ <- userFicRepo.patchDetails(record.key, details)
  } yield record.copy(details = details)

  override def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord] = for {
    _        <- userFicRepo.addComment(record.key, comment)
    comments <- userFicRepo.getAllComments(record.key)
  } yield record.copy(comments = comments)

  override def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord] = for {
    _     <- userFicRepo.addStartDate(record.key, startDate)
    dates <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def addFinishDate(record: UserFicRecord, finishDate: String): IO[Throwable, UserFicRecord] = for {
    _          <- userFicRepo.addFinishDate(record.key, finishDate)
    details    <- userFicRepo.getOrCreateDetails(record.key)
    newDetails <- ZIO.succeed(details.copy(read = true))
    _          <- if (!details.read) patchFicStats(record, newDetails) else ZIO.unit
    dates      <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(details = newDetails, readDatesInfo = dates)

  override def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] = for {
    _     <- userFicRepo.cancelStartedToday(record.key)
    dates <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  override def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] = for {
    _     <- userFicRepo.cancelFinishedToday(record.key)
    dates <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

object UserFicServiceImpl:
  def layer(db: KapibarabotDb): ZLayer[FicService, Nothing, UserFicServiceImpl] = ZLayer {
    for {
      ficService <- ZIO.service[FicService]
    } yield UserFicServiceImpl(db, ficService)
  }
