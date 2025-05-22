package kapibarabanka.lo3.api
package controllers

import services.StatsService

import kapibarabanka.lo3.common.lo3api.StatsClient
import zio.http.{Response, Route}

protected[api] case class StatsController() extends Controller:

  val tagStats = StatsClient.tagStats.implement { (userId, tagField) => StatsService.getTagStats(userId, tagField) }

  override val routes: List[Route[Any, Response]] = List(tagStats)
