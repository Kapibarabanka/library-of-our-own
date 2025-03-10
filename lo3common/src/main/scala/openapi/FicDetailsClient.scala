package kapibarabanka.lo3.common
package openapi

import models.domain.{Ao3FicInfo, Fic, FicDetails, Lo3Error, Note}

import kapibarabanka.lo3.common.models.api.FinishInfo
import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.Method.{GET, POST}
import zio.http.codec.*
import zio.http.endpoint.Endpoint

object FicDetailsClient extends MyClient:
  override protected val clientName = "fic-details"

  val patchDetails = (endpoint(Method.PATCH, "patch-details")
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey).in[FicDetails]

  val addNote = (endpoint(Method.PATCH, "add-note")
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey).in[Note]

  val startedToday = endpoint(Method.PATCH, "started-today")
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val finishedToday = endpoint(Method.PATCH, "finished-today")
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val abandonedToday = endpoint(Method.PATCH, "abandoned-today")
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey
  
  val finishFic = endpoint(POST, "finish-fic")
    .in[FinishInfo]
    .out[Unit]
    .outError[Lo3Error](Status.InternalServerError)

  override val allEndpoints =
    List(
      patchDetails,
      addNote,
      startedToday,
      finishedToday,
      abandonedToday,
      finishFic
    )
