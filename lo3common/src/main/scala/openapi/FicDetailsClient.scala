package kapibarabanka.lo3.common
package openapi

import models.api.{FicsPage, FicsPageRequest}
import models.domain.{FicComment, FicDetails, Lo3Error, UserFicRecord}

import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.Endpoint

object FicDetailsClient extends MyClient:
  override protected val clientName = "fic"

  val getUserFic = endpoint(Method.GET, "user-details")
    .query(HttpCodec.query[String]("ficLink"))
    .query(HttpCodec.query[String]("userId"))
    .query(HttpCodec.query[Boolean]("needToLog"))
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
  
  val getUserFicByKey = endpoint(Method.GET, "user-details-by-key")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val patchDetails = (endpoint(Method.PATCH, "patch-details")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey).in[FicDetails]

  val addComment = (endpoint(Method.PATCH, "add-comment")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey).in[FicComment]

  val startedToday = endpoint(Method.PATCH, "started-today")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val finishedToday = endpoint(Method.PATCH, "finished-today")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val cancelStartedToday = endpoint(Method.PATCH, "cancel-started-today")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  val cancelFinishedToday = endpoint(Method.PATCH, "cancel-finished-today")
    .out[UserFicRecord]
    .outError[Lo3Error](Status.InternalServerError)
    |> withKey

  override val allEndpoints =
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
