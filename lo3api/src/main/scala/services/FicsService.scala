package kapibarabanka.lo3.api
package services

import services.ao3Info.Ao3InfoService
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.{DbError, Fic, FicCard, Lo3Error, UserFicKey}
import kapibarabanka.lo3.common.services.{EmptyLog, OptionalLog}
import zio.{IO, ZIO}

class FicsService(ao3InfoService: Ao3InfoService):
  def getAllCards(userId: String): IO[DbError, List[FicCard]] = for {
    worksWithDetails  <- Lo3Data.works.getAllForUser(userId)
    seriesWithDetails <- Lo3Data.series.getAllForUser(userId, worksWithDetails.map((_, w) => w))
    workIdsFromSeries <- ZIO.succeed(seriesWithDetails.flatMap((_, _, ids) => ids).toSet)
    allFicsWithDetails <- ZIO.succeed(
      seriesWithDetails.map((d, s, _) => (d, s)) ++ worksWithDetails
        .filter((_, w) => !workIdsFromSeries.contains(w.id))
    )
  } yield allFicsWithDetails
    .map((details, fic) => FicCard(UserFicKey(userId, fic.id, fic.ficType), fic, details))

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
