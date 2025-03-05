package kapibarabanka.lo3.api
package controllers

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.openapi.FicDetailsClient
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
