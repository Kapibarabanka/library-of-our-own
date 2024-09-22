package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.domain.{FicComment, FicKey, ReadDates}
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.{CommentDoc, ReadDatesDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{CommentsTable, ReadDatesTable}
import slick.jdbc.PostgresProfile.api.*
import zio.IO

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

  def getReadDates(key: FicKey): IO[Throwable, List[ReadDates]] = for {
    dates <- db(
      readDates.filter(d => d.ficId === key.ficId && d.ficIsSeries === key.isSeries).result
    )
  } yield dates.map(_.toModel).toList
