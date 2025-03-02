package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.ReadDatesDoc
import sqlite.services.Lo3Db
import sqlite.tables.ReadDatesTable

import kapibarabanka.lo3.common.models.domain.{DbError, ReadDatesInfo, UserFicKey}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import java.time.LocalDate

class ReadDatesRepo(db: Lo3Db):
  private val readDates = TableQuery[ReadDatesTable]

  def getReadDatesInfo(key: UserFicKey): IO[DbError, ReadDatesInfo] = for {
    dates     <- db.run(filterDates(key).sortBy(_.id).result)
    maybeLast <- ZIO.succeed[Option[ReadDatesDoc]](dates.lastOption)
  } yield ReadDatesInfo(
    readDates = dates.map(_.toModel).toList,
    canStart = canStart(maybeLast),
    canFinish = canFinish(maybeLast)
  )

  def addStartDate(key: UserFicKey, startDate: String): IO[DbError, Unit] = for {
    _ <- db.run(readDates += ReadDatesDoc(None, key.userId, key.ficId, key.ficIsSeries, Some(startDate), None, false))
  } yield ()

  def addFinishDate(key: UserFicKey, endDate: String): IO[DbError, Unit] =
    for {
      startDates <- db.run(
        filterDates(key)
          .filter(d => d.endDate.isEmpty)
          .sortBy(_.startDate.desc)
          .result
      )
      _ <- startDates.headOption match
        case Some(startDateDoc) => db.run(readDates.filter(_.id === startDateDoc.id).map(_.endDate).update(Some(endDate)))
        case None => db.run(readDates += ReadDatesDoc(None, key.userId, key.ficId, key.ficIsSeries, None, Some(endDate), false))
    } yield ()

  def setIsAbandoned(key: UserFicKey, value: Boolean): IO[DbError, Unit] =
    for {
      startDates <- db.run(
        filterDates(key)
          .sortBy(_.startDate.desc)
          .result
      )
      _ <- startDates.headOption match
        case Some(startDateDoc) => db.run(readDates.filter(_.id === startDateDoc.id).map(_.isAbandoned).update(value))
        case None               => ZIO.unit
    } yield ()

  private def canStart(maybeLastDate: Option[ReadDatesDoc]) =
    maybeLastDate match
      case None      => true
      case Some(doc) => doc.endDate.isDefined

  private def canFinish(maybeLastDate: Option[ReadDatesDoc]) =
    maybeLastDate match
      case None      => false
      case Some(doc) => doc.endDate.isEmpty

  private def lastDatesRecord(key: UserFicKey) = filterDates(key).sortBy(_.id.desc).take(1)

  private def filterDates(key: UserFicKey) =
    readDates.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)
