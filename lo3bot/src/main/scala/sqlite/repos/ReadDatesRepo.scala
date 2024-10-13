package kapibarabanka.lo3.bot
package sqlite.repos

import domain.{ReadDatesInfo, UserFicKey}
import sqlite.SqliteError
import sqlite.docs.ReadDatesDoc
import sqlite.services.KapibarabotDb
import sqlite.tables.ReadDatesTable
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import java.time.LocalDate

class ReadDatesRepo(db: KapibarabotDb):
  private val readDates = TableQuery[ReadDatesTable]

  def getReadDatesInfo(key: UserFicKey): IO[SqliteError, ReadDatesInfo] = for {
    dates     <- db.run(filterDates(key).sortBy(_.id).result)
    maybeLast <- ZIO.succeed[Option[ReadDatesDoc]](dates.lastOption)
  } yield ReadDatesInfo(
    readDates = dates.map(_.toModel).toList,
    canAddStart = canAddStart(maybeLast),
    canAddFinish = canAddFinish(maybeLast),
    canCancelStart = canCancelStart(maybeLast),
    canCancelFinish = canCancelFinish(maybeLast)
  )

  def addStartDate(key: UserFicKey, startDate: String): IO[SqliteError, Unit] = for {
    _ <- db.run(readDates += ReadDatesDoc(None, key.userId, key.ficId, key.ficIsSeries, Some(startDate), None))
  } yield ()

  def addFinishDate(key: UserFicKey, endDate: String): IO[SqliteError, Unit] =
    for {
      startDates <- db.run(
        filterDates(key)
          .filter(d => d.endDate.isEmpty)
          .sortBy(_.startDate.desc)
          .result
      )
      _ <- startDates.headOption match
        case Some(startDateDoc) => db.run(readDates.filter(_.id === startDateDoc.id).map(_.endDate).update(Some(endDate)))
        case None => db.run(readDates += ReadDatesDoc(None, key.userId, key.ficId, key.ficIsSeries, None, Some(endDate)))
    } yield ()

  def cancelStartedToday(key: UserFicKey): IO[SqliteError, Unit] = for {
    maybeLast <- db.run(lastDatesRecord(key).result).map(_.headOption)
    _         <- if (canCancelStart(maybeLast)) db.run(readDates.filter(d => d.id === maybeLast.get.id).delete).unit else ZIO.unit
  } yield ()

  def cancelFinishedToday(key: UserFicKey): IO[SqliteError, Unit] = for {
    maybeLast <- db.run(lastDatesRecord(key).result).map(_.headOption)
    _ <-
      if (canCancelFinish(maybeLast))
        maybeLast.get.startDate match
          case None    => db.run(readDates.filter(d => d.id === maybeLast.get.id).delete).unit
          case Some(_) => db.run(readDates.filter(d => d.id === maybeLast.get.id).map(_.endDate).update(None)).unit
      else ZIO.unit
  } yield ()

  private def canAddStart(maybeLastDate: Option[ReadDatesDoc]) =
    val today = LocalDate.now().toString
    maybeLastDate match
      case None => true
      case Some(doc) =>
        doc.startDate match
          case Some(start) => start != today
          case None        => doc.endDate.getOrElse("") != today

  private def canAddFinish(maybeLastDate: Option[ReadDatesDoc]) =
    val today = LocalDate.now().toString
    maybeLastDate match
      case None      => true
      case Some(doc) => doc.endDate.getOrElse("") != today

  private def canCancelStart(maybeLastDate: Option[ReadDatesDoc]) =
    val today = LocalDate.now().toString
    maybeLastDate match
      case None      => false
      case Some(doc) => doc.startDate.getOrElse("") == today && doc.endDate.isEmpty

  private def canCancelFinish(maybeLastDate: Option[ReadDatesDoc]) =
    val today = LocalDate.now().toString
    maybeLastDate match
      case None      => false
      case Some(doc) => doc.endDate.getOrElse("") == today

  private def lastDatesRecord(key: UserFicKey) = filterDates(key).sortBy(_.id.desc).take(1)

  private def filterDates(key: UserFicKey) =
    readDates.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)
