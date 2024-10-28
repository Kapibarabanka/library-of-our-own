package kapibarabanka.lo3.common
package openapi

import models.ao3.{Series, Work}

import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.PathCodec.{string, *}

object Ao3Client extends MyClient:
  override protected val clientName = "ao3"

  val workById = endpoint(Method.POST, "work" / string("workId"))
    .out[Work]
    |> withStringError

  val seriesById = endpoint(Method.POST, "series" / string("seriesId"))
    .out[Series]
    |> withStringError

  val ficByLink = endpoint(Method.POST, "fic")
    .in[String]
    .out[Work]
    .out[Series]
    |> withStringError

  val downloadLink = endpoint(Method.GET, "download-link")
    .query(HttpCodec.query[String]("workId"))
    .out[String]
    |> withStringError

  override val allEndpoints = List(ficByLink, workById, seriesById, downloadLink)
