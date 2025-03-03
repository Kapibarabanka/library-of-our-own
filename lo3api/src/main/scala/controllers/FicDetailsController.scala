package kapibarabanka.lo3.api
package controllers

import services.FicsService
import services.ao3Info.Ao3InfoService
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType}
import kapibarabanka.lo3.common.models.domain.{Fic, Lo3Error, NotAo3Link, UserFicKey}
import kapibarabanka.lo3.common.openapi.FicDetailsClient
import kapibarabanka.lo3.common.services.{EmptyLog, LogMessage, MyBotApi, OptionalLog}
import zio.*
import zio.http.*

import java.time.LocalDate

protected[api] case class FicDetailsController() extends Controller:
  val patchDetails = FicDetailsClient.patchDetails.implement { (key, details) => Lo3Data.details.patchDetails(key, details) }

  val addNote = FicDetailsClient.addNote.implement { (key, note) => Lo3Data.notes.addNote(key, note) }

  val startedToday = FicDetailsClient.startedToday.implement { key =>
    Lo3Data.readDates.addStartDate(key, LocalDate.now().toString)
  }

  val finishedToday = FicDetailsClient.finishedToday.implement { key =>
    for {
      _ <- Lo3Data.readDates.addFinishDate(key, LocalDate.now().toString)
      _ <- Lo3Data.details.setBacklog(key, false)
    } yield ()
  }

  val abandonedToday = FicDetailsClient.abandonedToday.implement { key =>
    for {
      _ <- Lo3Data.readDates.addFinishDate(key, LocalDate.now().toString)
      _ <- Lo3Data.readDates.setIsAbandoned(key, true)
      _ <- Lo3Data.details.setBacklog(key, false)
    } yield ()
  }

  override val routes: List[Route[Any, Response]] =
    List(
      patchDetails,
      addNote,
      startedToday,
      finishedToday,
      abandonedToday
    )
