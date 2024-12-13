package kapibarabanka.lo3.common
package openapi

import models.ao3.{Ao3Error, Series, Work}

import scalaz.Scalaz.ToIdOps
import zio.http.*
import zio.http.codec.HttpCodec
import zio.http.codec.PathCodec.{string, *}

object Ao3Client extends MyClient:
  override protected val clientName = "ao3"

  val workById = endpoint(Method.POST, "work" / string("workId"))
    .out[Work]
    .outError[Ao3Error](Status.InternalServerError)

  val seriesById = endpoint(Method.POST, "series" / string("seriesId"))
    .out[Series]
    .outError[Ao3Error](Status.InternalServerError)

  val ficByLink = endpoint(Method.POST, "fic")
    .in[String]
    .out[Work]
    .out[Series]
    .outError[Ao3Error](Status.InternalServerError)

  val downloadLink = endpoint(Method.GET, "download-link")
    .query(HttpCodec.query[String]("workId"))
    .out[String]
    .outError[Ao3Error](Status.InternalServerError)

  override val allEndpoints = List(ficByLink, workById, seriesById, downloadLink)
