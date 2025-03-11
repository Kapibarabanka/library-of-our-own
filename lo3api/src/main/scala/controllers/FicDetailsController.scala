package kapibarabanka.lo3.api
package controllers

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.domain.Note
import kapibarabanka.lo3.common.openapi.FicDetailsClient
import zio.*
import zio.http.*

import java.time.{LocalDate, LocalDateTime}

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

  val finishFic = FicDetailsClient.finishFic.implement { finishInfo =>
    for {
      _ <- Lo3Data.readDates.addFinishDate(finishInfo.key, LocalDate.now().toString)
      _ <- Lo3Data.readDates.setIsAbandoned(finishInfo.key, finishInfo.abandoned)
      _ <- finishInfo.impression.map(impression => Lo3Data.details.setImpression(finishInfo.key, impression)).getOrElse(ZIO.unit)
      _ <- finishInfo.note
        .map(text => Lo3Data.notes.addNote(finishInfo.key, Note(None, LocalDateTime.now(), text)))
        .getOrElse(ZIO.unit)
      _ <- Lo3Data.details.setBacklog(finishInfo.key, false)
    } yield ()
  }

  override val routes: List[Route[Any, Response]] =
    List(
      patchDetails,
      addNote,
      startedToday,
      finishedToday,
      abandonedToday,
      finishFic
    )
