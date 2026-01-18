package kapibarabanka.lo3.api
package controllers

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.domain.Note
import kapibarabanka.lo3.common.lo3api.FicDetailsClient
import zio.*
import zio.http.*

import java.time.{LocalDate, LocalDateTime}

protected[api] case class FicDetailsController() extends Controller:
  val patchDetails = FicDetailsClient.patchDetails.implement { (key, details) => Lo3Data.details.patchDetails(key, details) }

  val addNote = FicDetailsClient.addNote.implement { (key, note) => Lo3Data.notes.addNote(key, note) }

  val startedToday = FicDetailsClient.startedToday.implement { key =>
    Lo3Data.readDates.addStartDate(key, LocalDate.now().toString)
  }

  val finishFic = FicDetailsClient.finishFic.implement { finishInfo =>
    for {
      _       <- Lo3Data.readDates.addFinishDate(finishInfo.key, LocalDate.now().toString, finishInfo.abandoned)
      details <- Lo3Data.details.getOrCreateDetails(finishInfo.key)
      newDetails <- ZIO.succeed(
        details.copy(backlog = false, impression = finishInfo.impression.orElse(details.impression), spicy = finishInfo.spicy)
      )
      _ <- Lo3Data.details.patchDetails(finishInfo.key, newDetails)
      _ <- finishInfo.note
        .map(text => Lo3Data.notes.addNote(finishInfo.key, Note(None, LocalDateTime.now(), text)))
        .getOrElse(ZIO.unit)
    } yield ()
  }

  override val routes: List[Route[Any, Response]] =
    List(
      patchDetails,
      addNote,
      startedToday,
      finishFic
    )
