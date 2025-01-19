package kapibarabanka.lo3.api
package controllers

import ficService.FicService

import kapibarabanka.lo3.common.models.ao3
import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType}
import kapibarabanka.lo3.common.models.domain.{Lo3Error, NotAo3Link, UserFicKey, UserFicRecord}
import kapibarabanka.lo3.common.openapi.FicDetailsClient
import kapibarabanka.lo3.common.services.{EmptyLog, LogMessage, MyBotApi, OptionalLog}
import zio.*
import zio.http.*

import java.time.LocalDate

protected[api] case class FicDetailsController(ficService: FicService, bot: MyBotApi) extends Controller:
  val getUserFic = FicDetailsClient.getUserFic.implement { (ficLink, userId, needToLog) =>
    Ao3Url.tryParseFicLink(ficLink) match
      case None => ZIO.fail(NotAo3Link(ficLink))
      case Some((ficId, ficType)) =>
        for {
          log    <- if (needToLog) LogMessage.create("Working on it...", bot, userId) else ZIO.succeed(EmptyLog())
          result <- getUserFicInternal(UserFicKey(userId, ficId, ficType), log)
          _      <- log.delete
        } yield result
  }

  val updateFic = FicDetailsClient.updateFic.implement { key =>
    for {
      log <- if (key.userId.nonEmpty) LogMessage.create("Working on it...", bot, key.userId) else ZIO.succeed(EmptyLog())
      result <- ficService.updateFic(key.ficId, key.ficType, log)
      _ <- log.delete
    } yield result
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

  val finishedToday = FicDetailsClient.finishedToday.implement { key =>
    for {
      _             <- data.readDates.addFinishDate(key, LocalDate.now().toString)
      _             <- data.details.setBacklog(key, false)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val cancelStartedToday = FicDetailsClient.cancelStartedToday.implement { key =>
    for {
      _             <- data.readDates.cancelStartedToday(key)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  val cancelFinishedToday = FicDetailsClient.cancelFinishedToday.implement { key =>
    for {
      _             <- data.readDates.cancelFinishedToday(key)
      patchedRecord <- getUserFicInternal(key)
    } yield patchedRecord
  }

  private def getUserFicInternal(key: UserFicKey, log: OptionalLog = EmptyLog()): IO[Lo3Error, UserFicRecord] = for {
    fic           <- ficService.getFic(key.ficId, key.ficType, log)
    _             <- if (key.ficIsSeries) createDetailsForSeriesParts(key) else ZIO.unit
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

  private def createDetailsForSeriesParts(key: UserFicKey) = for {
    ids <- data.series.workIds(key.ficId)
    _   <- ZIO.collectAll(ids.map(id => data.details.getOrCreateDetails(UserFicKey(key.userId, id, FicType.Work))))
  } yield ()

  override val routes: List[Route[Any, Response]] =
    List(
      getUserFic,
      updateFic,
      getUserFicByKey,
      patchDetails,
      addComment,
      startedToday,
      finishedToday,
      cancelStartedToday,
      cancelFinishedToday
    )
