package kapibarabanka.lo3.api
package controllers

import services.StatsService

import kapibarabanka.lo3.common.lo3api.StatsClient
import zio.http.{Response, Route}

protected[api] case class StatsController() extends Controller:

  val tagFieldStats = StatsClient.tagFieldStats.implement { (userId, tagField) =>
    StatsService.getTagFieldStats(userId, tagField)
  }

  override val routes: List[Route[Any, Response]] = List(tagFieldStats)
