package kapibarabanka.lo3.api
package controllers

import ao3scrapper.{Ao3, Ao3Url}

import kapibarabanka.lo3.models.ao3
import kapibarabanka.lo3.models.ao3.FicType
import kapibarabanka.lo3.models.openapi.FicDetailsClient
import kapibarabanka.lo3.models.tg.{FlatFicModel, UserFicKey, UserFicRecord}
import zio.*
import zio.http.*

import java.time.LocalDate

protected[api] case class FicDetailsController(ao3: Ao3) extends Controller:
  val getUserFic = FicDetailsClient.getUserFic.implement { (ficLink, userId) =>
    Ao3Url.tryParseFicLink(ficLink) match
      case None                   => ZIO.fail(s"$ficLink is not a parsable AO3 link")
      case Some((ficId, ficType)) => getUserFicInternal(UserFicKey(userId, ficId, ficType))
  }

  val getUserFicByKey = FicDetailsClient.getUserFicByKey.implement { key => getUserFicInternal(key) }

  val patchDetails = FicDetailsClient.patchDetails.implement { (key, details) =>
    for {
      _             <- data.details.patchDetails(key, details)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val addComment = FicDetailsClient.addComment.implement { (key, comment) =>
    for {
      _             <- data.comments.addComment(key, comment)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val startedToday = FicDetailsClient.startedToday.implement { key =>
    for {
      _             <- data.readDates.addStartDate(key, LocalDate.now().toString)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val finishedToday = FicDetailsClient.startedToday.implement { key =>
    for {
      _             <- data.readDates.addFinishDate(key, LocalDate.now().toString)
      _             <- data.details.setBacklog(key, false)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val cancelStartedToday = FicDetailsClient.startedToday.implement { key =>
    for {
      _             <- data.readDates.cancelStartedToday(key)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val cancelFinishedToday = FicDetailsClient.startedToday.implement { key =>
    for {
      _             <- data.readDates.cancelFinishedToday(key)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  private def getUserFicInternal(key: UserFicKey): IO[String, UserFicRecord] = for {
    maybeFic <- key.ficType match
      case FicType.Work   => data.works.getById(key.ficId)
      case FicType.Series => data.series.getById(key.ficId)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None      => parseFic(key.ficId, key.ficType)
    details       <- data.details.getOrCreateDetails(key)
    readDatesInfo <- data.readDates.getReadDatesInfo(key)
    comments      <- data.comments.getAllComments(key)
  } yield UserFicRecord(
    userId = key.userId,
    fic = fic,
    readDatesInfo = readDatesInfo,
    comments = comments,
    details = details
  )

  private def parseFic(ficId: String, ficType: FicType): IO[String, FlatFicModel] = ficType match
    case FicType.Work   => ao3.work(ficId).mapError(e => e.getMessage).flatMap(work => data.works.add(work))
    case FicType.Series => ao3.series(ficId).mapError(e => e.getMessage).flatMap(series => data.series.add(series))

  override val routes: List[Route[Any, Response]] =
    List(
      getUserFic,
      getUserFicByKey,
      patchDetails,
      addComment,
      startedToday,
      finishedToday,
      cancelStartedToday,
      cancelFinishedToday
    )
