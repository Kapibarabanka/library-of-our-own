package kapibarabanka.lo3.common
package openapi

import models.api.MonthStats
import models.domain.Lo3Error
import openapi.KindleClient.endpoint

import zio.http.Method.GET
import zio.http.codec.PathCodec.string
import zio.http.{Method, Status}

object StatsClient extends MyClient:
  override protected val clientName = "stats"

//  val generalStats = endpoint(GET, string("userId") / "general-stats")
//    .out[List[MonthStats]]
//    .outError[Lo3Error](Status.InternalServerError)

  override val allEndpoints = List()
