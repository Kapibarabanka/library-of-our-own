package kapibarabanka.lo3.common
package openapi

import models.api.{StatTagField, TagFieldStats}
import models.domain.Lo3Error

import zio.http.Method.GET
import zio.http.Status
import zio.http.codec.PathCodec.string

object StatsClient extends MyClient:
  override protected val clientName = "stats"

  val tagStats = endpoint(GET, string("userId") / string("tagField") / "stats")
    .transformIn { case (userId, tagStr) => (userId, StatTagField.valueOf(tagStr.toLowerCase.capitalize)) } {
      (userId, tagField) => (userId, tagField.toString)
    }
    .out[TagFieldStats]
    .outError[Lo3Error](Status.InternalServerError)

  override val allEndpoints = List(tagStats)
