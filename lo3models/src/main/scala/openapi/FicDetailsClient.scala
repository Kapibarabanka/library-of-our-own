package kapibarabanka.lo3.models
package openapi

import tg.*

import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.Endpoint

object FicDetailsClient extends MyClient:
  override protected val clientName = "fic"

  val getUserFic = endpoint(Method.GET, "user-details")
    .query(HttpCodec.query[String]("ficLink"))
    .query(HttpCodec.query[String]("userId"))
    .out[UserFicRecord]
    |> withStringError

  val getUserFicByKey = endpoint(Method.GET, "user-details-by-key")
    .out[UserFicRecord]
    |> withStringError |> withKey

  val patchDetails = (endpoint(Method.PATCH, "patch-details")
    .out[UserFicRecord]
    |> withStringError
    |> withKey).in[FicDetails]

  val addComment = (endpoint(Method.PATCH, "add-comment")
    .out[UserFicRecord]
    |> withStringError
    |> withKey).in[FicComment]

  val startedToday = endpoint(Method.PATCH, "started-today")
    .out[UserFicRecord]
    |> withStringError
    |> withKey

  val finishedToday = endpoint(Method.PATCH, "finished-today")
    .out[UserFicRecord]
    |> withStringError
    |> withKey

  val cancelStartedToday = endpoint(Method.PATCH, "cancel-started-today")
    .out[UserFicRecord]
    |> withStringError
    |> withKey

  val cancelFinishedToday = endpoint(Method.PATCH, "cancel-finished-today")
    .out[UserFicRecord]
    |> withStringError
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
