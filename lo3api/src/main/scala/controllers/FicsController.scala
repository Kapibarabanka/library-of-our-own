package kapibarabanka.lo3.api
package controllers

import services.FicsService
import services.ao3Info.Ao3InfoService

import kapibarabanka.lo3.common.models.ao3.Ao3Url
import kapibarabanka.lo3.common.models.api.HomePageData
import kapibarabanka.lo3.common.models.domain.{NotAo3Link, UserFicKey}
import kapibarabanka.lo3.common.openapi.FicsClient
import kapibarabanka.lo3.common.services.{EmptyLog, LogMessage, MyBotApi}
import zio.ZIO
import slick.jdbc.PostgresProfile.api.*
import zio.http.{Response, Route}

import scala.util.Random

protected[api] case class FicsController(ao3InfoService: Ao3InfoService, bot: MyBotApi) extends Controller:
  private val ficsService = FicsService(ao3InfoService)

  val getAllCards = FicsClient.getAllCards.implement { userId => ficsService.getAllCards(userId) }

  val getFicByLink = FicsClient.getFicByLink.implement { (ficLink, userId, needToLog) =>
    Ao3Url.tryParseFicLink(ficLink) match
      case None => ZIO.fail(NotAo3Link(ficLink))
      case Some((ficId, ficType)) =>
        for {
          log    <- if (needToLog) LogMessage.create("Working on it...", bot, userId) else ZIO.succeed(EmptyLog())
          result <- ficsService.getFic(UserFicKey(userId, ficId, ficType), log)
          _      <- log.delete
        } yield result
  }

  val getFicByKey = FicsClient.getFicByKey.implement { key => ficsService.getFic(key) }

  val getHomePage = FicsClient.getHomePage.implement { userId =>
    val random = new Random()
    for {
      started       <- ficsService.getStarted(userId)
      ficsInBacklog <- ficsService.getBacklog(userId)
      randomFic     <- ZIO.succeed(if (ficsInBacklog.isEmpty) None else Some(ficsInBacklog(random.nextInt(ficsInBacklog.length))))
    } yield HomePageData(currentlyReading = started, randomFicFromBacklog = randomFic)
  }

  val updateAo3Info = FicsClient.updateAo3Info.implement { key =>
    for {
      log    <- if (key.userId.nonEmpty) LogMessage.create("Working on it...", bot, key.userId) else ZIO.succeed(EmptyLog())
      result <- ao3InfoService.updateAo3Info(key.ficId, key.ficType, log)
      _      <- log.delete
    } yield result
  }

  override val routes: List[Route[Any, Response]] = List(getAllCards, getFicByLink, getFicByKey, getHomePage, updateAo3Info)
