package kapibarabanka.lo3.api
package controllers

import kapibarabanka.lo3.common.models.api.{FicCard, FicsPage}
import kapibarabanka.lo3.common.models.domain.UserFicKey
import kapibarabanka.lo3.common.openapi.CardsClient
import zio.ZIO
import zio.http.{Response, Route}

protected[api] case class CardsController() extends Controller:
  val getFicsPage = CardsClient.getFicsPage.implement { request =>
    val startIndex = request.pageSize * request.pageNumber
    val endIndex   = startIndex + request.pageSize
    for {
//      seriesWithDetails <- data.series.getAllForUser(request.userId)
      // todo: filter out works that are contained in the series
      worksWithDetails <- data.works.getAllForUser(request.userId)
      allCards <- ZIO.succeed(
        (worksWithDetails).map((details, fic) => FicCard(UserFicKey(request.userId, fic.id, fic.ficType), fic, details))
      )
    } yield FicsPage(total = allCards.length, cards = allCards.slice(startIndex, endIndex))
  }

  override val routes: List[Route[Any, Response]] = List(getFicsPage)
