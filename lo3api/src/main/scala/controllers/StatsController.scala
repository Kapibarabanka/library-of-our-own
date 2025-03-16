package kapibarabanka.lo3.api
package controllers

import kapibarabanka.lo3.common.models.api.MonthStats
import kapibarabanka.lo3.common.openapi.StatsClient
import zio.ZIO
import zio.http.{Response, Route}

protected[api] case class StatsController() extends Controller:
//  val generalStats = StatsClient.generalStats.implement { userId =>
//    ZIO.succeed(List(MonthStats("Jan", 12, 65), MonthStats("Feb", 7, 123), MonthStats("Mar", 15, 56)))
//  }

  override val routes: List[Route[Any, Response]] = List(generalStats)
