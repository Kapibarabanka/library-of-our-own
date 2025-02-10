package kapibarabanka.lo3.api
package controllers

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.api.{FicCard, FicsPage}
import kapibarabanka.lo3.common.models.domain.UserFicKey
import kapibarabanka.lo3.common.openapi.CardsClient
import zio.ZIO
import zio.http.{Response, Route}

protected[api] case class CardsController() extends Controller:
  val getAllFics = CardsClient.getAllFics.implement { userId => for {
      //      seriesWithDetails <- data.series.getAllForUser(request.userId)
      // todo: filter out works that are contained in the series
      worksWithDetails <- Lo3Data.works.getAllForUser(userId)
      allCards <- ZIO.succeed(
        worksWithDetails.map((details, fic) => FicCard(UserFicKey(userId, fic.id, fic.ficType), fic, details))
      )
    } yield FicsPage(total = allCards.length, cards = allCards)
  }

  val getFicsPage = CardsClient.getFicsPage.implement { request =>
    val startIndex = request.pageSize * request.pageNumber
    val endIndex   = startIndex + request.pageSize
    for {
//      seriesWithDetails <- data.series.getAllForUser(request.userId)
      // todo: filter out works that are contained in the series
      worksWithDetails <- Lo3Data.works.getAllForUser(request.userId)
      allCards <- ZIO.succeed(
        (worksWithDetails).map((details, fic) => FicCard(UserFicKey(request.userId, fic.id, fic.ficType), fic, details))
      )
    } yield FicsPage(total = allCards.length, cards = allCards.slice(startIndex, endIndex))
  }

  override val routes: List[Route[Any, Response]] = List(getAllFics, getFicsPage)
