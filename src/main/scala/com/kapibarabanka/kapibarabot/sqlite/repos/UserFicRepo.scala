package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDetails, ReadDatesInfo, UserFicKey}
import com.kapibarabanka.kapibarabot.sqlite.SqliteOld
import com.kapibarabanka.kapibarabot.sqlite.docs.{CommentDoc, FicDetailsDoc, ReadDatesDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{CommentsTable, FicsDetailsTable, ReadDatesTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import java.time.LocalDate

class UserFicRepo:
  private val comments    = TableQuery[CommentsTable]
  private val readDates   = TableQuery[ReadDatesTable]
  private val ficsDetails = TableQuery[FicsDetailsTable]

  def addUserFicRecord(key: UserFicKey): IO[Throwable, FicDetails] = for {
    _ <- SqliteOld.run(
      ficsDetails += FicDetailsDoc(
        id = None,
        userId = key.userId,
        ficId = key.ficId,
        ficIsSeries = key.ficIsSeries,
        read = false,
        backlog = false,
        isOnKindle = false,
        quality = None,
        fire = false,
        recordCreated = LocalDate.now().toString
      )
    )
    maybeDetails <- getDetailsOption(key)
  } yield maybeDetails.get

  def getDetailsOption(key: UserFicKey): IO[Throwable, Option[FicDetails]] = for {
    docs <- SqliteOld.run(filterDetails(key).result)
  } yield docs.headOption.map(_.toModel)

  def getOrCreateDetails(key: UserFicKey): IO[Throwable, FicDetails] = for {
    maybeDetails <- getDetailsOption(key)
    details <- maybeDetails match
      case Some(value) => ZIO.succeed(value)
      case None        => addUserFicRecord(key)
  } yield details

  // TODO: when marking series as read or on kindle mark all the works with same values
  def patchDetails(key: UserFicKey, details: FicDetails) = for {
    _ <- SqliteOld.run(
      filterDetails(key)
        .map(d => (d.read, d.backlog, d.isOnKindle, d.quality, d.fire))
        .update(
          (
            details.read,
            details.backlog,
            details.isOnKindle,
            details.quality.map(_.toString),
            details.fire
          )
        )
    )
  } yield ()

  def addComment(key: UserFicKey, comment: FicComment): IO[Throwable, Unit] = SqliteOld
    .run(
      comments += CommentDoc(None, key.userId, key.ficId, key.ficIsSeries, comment.commentDate, comment.comment)
    )
    .unit

  def getAllComments(key: UserFicKey): IO[Throwable, List[FicComment]] = for {
    comments <- SqliteOld.run(filterComments(key).result)
  } yield comments.map(_.toModel).toList.sortBy(_.commentDate)

  def addStartDate(key: UserFicKey, startDate: String): IO[Throwable, Unit] = for {
    _ <- SqliteOld.run(readDates += ReadDatesDoc(None, key.userId, key.ficId, key.ficIsSeries, Some(startDate), None))
  } yield ()

  def addFinishDate(key: UserFicKey, endDate: String): IO[Throwable, Unit] =
    for {
      startDates <- SqliteOld.run(
        filterDates(key)
          .filter(d => d.endDate.isEmpty)
          .sortBy(_.startDate.desc)
          .result
      )
      _ <- startDates.headOption match
        case Some(startDateDoc) => SqliteOld.run(readDates.filter(_.id === startDateDoc.id).map(_.endDate).update(Some(endDate)))
        case None => SqliteOld.run(readDates += ReadDatesDoc(None, key.userId, key.ficId, key.ficIsSeries, None, Some(endDate)))
    } yield ()

  def getReadDatesInfo(key: UserFicKey): IO[Throwable, ReadDatesInfo] = for {
    dates     <- SqliteOld.run(filterDates(key).sortBy(_.id).result)
    maybeLast <- ZIO.succeed[Option[ReadDatesDoc]](dates.lastOption)
  } yield ReadDatesInfo(
    readDates = dates.map(_.toModel).toList,
    canAddStart = canAddStart(maybeLast),
    canAddFinish = canAddFinish(maybeLast),
    canCancelStart = canCancelStart(maybeLast),
    canCancelFinish = canCancelFinish(maybeLast)
  )

  def cancelStartedToday(key: UserFicKey): IO[Throwable, Unit] = for {
    maybeLast <- SqliteOld.run(lastDatesRecord(key).result).map(_.headOption)
    _ <- if (canCancelStart(maybeLast)) SqliteOld.run(readDates.filter(d => d.id === maybeLast.get.id).delete).unit else ZIO.unit
  } yield ()

  def cancelFinishedToday(key: UserFicKey): IO[Throwable, Unit] = for {
    maybeLast <- SqliteOld.run(lastDatesRecord(key).result).map(_.headOption)
    _ <-
      if (canCancelFinish(maybeLast))
        maybeLast.get.startDate match
          case None    => SqliteOld.run(readDates.filter(d => d.id === maybeLast.get.id).delete).unit
          case Some(_) => SqliteOld.run(readDates.filter(d => d.id === maybeLast.get.id).map(_.endDate).update(None)).unit
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

  private def filterDetails(key: UserFicKey) =
    ficsDetails.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)

  private def filterDates(key: UserFicKey) =
    readDates.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)

  private def filterComments(key: UserFicKey) =
    comments.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)
