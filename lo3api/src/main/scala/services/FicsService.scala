package kapibarabanka.lo3.api
package services

import services.ao3Info.Ao3InfoService
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.{DbError, Fic, FicCard, Lo3Error, UserFicKey}
import kapibarabanka.lo3.common.services.{EmptyLog, OptionalLog}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class FicsService(ao3InfoService: Ao3InfoService):
  def getAllCards(userId: String): IO[DbError, List[FicCard]] = Lo3Data.fics.getFilteredCards(userId, None, None)

  def getBacklog(userId: String): IO[DbError, List[FicCard]] =
    Lo3Data.fics.getFilteredCards(userId, Some(details => details.backlog === true), None)

  def getStarted(userId: String): IO[DbError, List[FicCard]] =
    Lo3Data.fics.getFilteredCards(userId, None, Some(dates => dates.startDate.nonEmpty && dates.endDate.isEmpty))

  def getFic(key: UserFicKey, log: OptionalLog = EmptyLog()): IO[Lo3Error, Fic] = for {
    fic           <- ao3InfoService.getAo3Info(key.ficId, key.ficType, log)
    _             <- if (key.ficIsSeries) createDetailsForSeriesParts(key) else ZIO.unit
    details       <- Lo3Data.details.getOrCreateDetails(key)
    readDatesInfo <- Lo3Data.readDates.getReadDatesInfo(key)
    notes         <- Lo3Data.notes.getAllNotes(key)
  } yield Fic(
    userId = key.userId,
    ao3Info = fic,
    readDatesInfo = readDatesInfo,
    notes = notes,
    details = details
  )

  private def createDetailsForSeriesParts(key: UserFicKey) = for {
    ids <- Lo3Data.series.workIds(key.ficId)
    _   <- ZIO.collectAll(ids.map(id => Lo3Data.details.getOrCreateDetails(UserFicKey(key.userId, id, FicType.Work))))
  } yield ()
