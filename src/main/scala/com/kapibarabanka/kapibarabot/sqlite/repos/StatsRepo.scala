package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.domain.{FicComment, FicKey, ReadDates, ReadDatesInfo}
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.{CommentDoc, ReadDatesDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{CommentsTable, ReadDatesTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import java.time.LocalDate

class StatsRepo(userId: String) extends WithDb(userId):
  private val comments  = TableQuery[CommentsTable]
  private val readDates = TableQuery[ReadDatesTable]

  def addComment(key: FicKey, comment: FicComment): IO[Throwable, Unit] = db(
    comments += CommentDoc(None, key.ficId, key.isSeries, comment.commentDate, comment.comment)
  ).unit

  def getAllComments(key: FicKey): IO[Throwable, List[FicComment]] = for {
    comments <- db(comments.filter(c => c.ficId === key.ficId && c.ficIsSeries === key.isSeries).result)
  } yield comments.map(_.toModel).toList.sortBy(_.commentDate)

  def addStartDate(key: FicKey, startDate: String): IO[Throwable, Unit] = for {
    _ <- db(readDates += ReadDatesDoc(None, key.ficId, key.isSeries, Some(startDate), None))
  } yield ()

  def addFinishDate(key: FicKey, endDate: String): IO[Throwable, Unit] =
    for {
      startDates <- db(
        readDates
          .filter(d => d.ficId === key.ficId && d.ficIsSeries === key.isSeries && d.endDate.isEmpty)
          .sortBy(_.startDate.desc)
          .result
      )
      _ <- startDates.headOption match
        case Some(startDateDoc) => db(readDates.filter(_.id === startDateDoc.id).map(_.endDate).update(Some(endDate)))
        case None               => db(readDates += ReadDatesDoc(None, key.ficId, key.isSeries, None, Some(endDate)))
    } yield ()

  def getReadDatesInfo(key: FicKey): IO[Throwable, ReadDatesInfo] = for {
    dates <- db(
      readDates.filter(d => d.ficId === key.ficId && d.ficIsSeries === key.isSeries).sortBy(_.id).result
    )
    maybeLast <- ZIO.succeed[Option[ReadDatesDoc]](dates.lastOption)
  } yield ReadDatesInfo(
    readDates = dates.map(_.toModel).toList,
    canAddStart = canAddStart(maybeLast),
    canAddFinish = canAddFinish(maybeLast),
    canCancelStart = canCancelStart(maybeLast),
    canCancelFinish = canCancelFinish(maybeLast)
  )

  def cancelStartedToday(key: FicKey): IO[Throwable, Unit] = for {
    maybeLast <- db(lastDatesRecord(key).result).map(_.headOption)
    _         <- if (canCancelStart(maybeLast)) db(readDates.filter(d => d.id === maybeLast.get.id).delete).unit else ZIO.unit
  } yield ()

  def cancelFinishedToday(key: FicKey): IO[Throwable, Unit] = for {
    maybeLast <- db(lastDatesRecord(key).result).map(_.headOption)
    _ <-
      if (canCancelFinish(maybeLast))
        maybeLast.get.startDate match
          case None    => db(readDates.filter(d => d.id === maybeLast.get.id).delete).unit
          case Some(_) => db(readDates.filter(d => d.id === maybeLast.get.id).map(_.endDate).update(None)).unit
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

  private def lastDatesRecord(key: FicKey) =
    readDates.filter(d => d.ficId === key.ficId && d.ficIsSeries === key.isSeries).sortBy(_.id.desc).take(1)
