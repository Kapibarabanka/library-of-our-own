package kapibarabanka.lo3.api
package controllers

import ao3scrapper.Ao3

import kapibarabanka.lo3.common.models.ao3
import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType, Series, Work}
import kapibarabanka.lo3.common.models.domain.{DbError, FlatFicModel, Lo3Error, NotAo3Link, UserFicKey, UserFicRecord}
import kapibarabanka.lo3.common.openapi.FicDetailsClient
import kapibarabanka.lo3.common.services.{EmptyLog, LogMessage, MyBotApi, OptionalLog}
import zio.*
import zio.http.*

import java.time.LocalDate

protected[api] case class FicDetailsController(ao3: Ao3, bot: MyBotApi) extends Controller:
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
    maybeFic <- key.ficType match
      case FicType.Work   => data.works.getById(key.ficId)
      case FicType.Series => data.series.getById(key.ficId)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None =>
        for {
          f <- parseFicAndSave(key.ficId, key.ficType, log)
          _ <- if (key.ficIsSeries) createDetailsForSeriesParts(key) else ZIO.unit
        } yield f
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

  private def parseFicAndSave(ficId: String, ficType: FicType, log: OptionalLog): IO[Lo3Error, FlatFicModel] =
    for {
      _            <- log.edit("Parsing AO3...")
      workOrSeries <- parseFic(ficId, ficType)
      _            <- log.edit("Saving to database...")
      fic <- workOrSeries match
        case work: Work     => data.works.add(work)
        case series: Series => data.series.add(series)
    } yield fic

  private def parseFic(ficId: String, ficType: FicType): IO[Lo3Error, Work | Series] = ficType match
    case FicType.Work   => ao3.work(ficId).mapError(e => Lo3Error.fromAo3Error(e))
    case FicType.Series => ao3.series(ficId).mapError(e => Lo3Error.fromAo3Error(e))

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
