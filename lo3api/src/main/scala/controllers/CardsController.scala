package kapibarabanka.lo3.api
package controllers

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.api.{FicCard, FicsPage}
import kapibarabanka.lo3.common.models.domain.UserFicKey
import kapibarabanka.lo3.common.openapi.CardsClient
import zio.ZIO
import zio.http.{Response, Route}

import java.time.{LocalDate, LocalDateTime}

protected[api] case class CardsController() extends Controller:
  val getAllFics = CardsClient.getAllFics.implement { userId =>
    getAllCards(userId).map(allCards => FicsPage(total = allCards.length, cards = allCards))
  }

  val getFicsPage = CardsClient.getFicsPage.implement { request =>
    val startIndex = request.pageSize * request.pageNumber
    val endIndex   = startIndex + request.pageSize
    getAllCards(request.userId).map(allCards => FicsPage(total = allCards.length, cards = allCards.slice(startIndex, endIndex)))
  }

  private def getAllCards(userId: String) = for {
    worksWithDetails  <- Lo3Data.works.getAllForUser(userId)
    seriesWithDetails <- Lo3Data.series.getAllForUser(userId, worksWithDetails.map((_, w) => w))
    workIdsFromSeries <- ZIO.succeed(seriesWithDetails.flatMap((_, _, ids) => ids).toSet)
    allFicsWithDetails <- ZIO.succeed(
      seriesWithDetails.map((d, s, _) => (d, s)) ++ worksWithDetails
        .filter((_, w) => !workIdsFromSeries.contains(w.id))
    )
  } yield allFicsWithDetails
    .map((details, fic) => FicCard(UserFicKey(userId, fic.id, fic.ficType), fic, details))
    .sortBy(c => c.details.recordCreated)(Ordering[LocalDateTime].reverse)

  override val routes: List[Route[Any, Response]] = List(getAllFics, getFicsPage)
