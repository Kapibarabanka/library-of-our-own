package kapibarabanka.lo3.api
package controllers

import services.{FicsService, StatsService}
import services.ao3Info.Ao3InfoService

import kapibarabanka.lo3.common.models.ao3.{Ao3Url, FicType}
import kapibarabanka.lo3.common.models.api.{HomePageData, MonthStats}
import kapibarabanka.lo3.common.models.domain.{NotAo3Link, UserFicKey}
import kapibarabanka.lo3.common.lo3api.FicsClient
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
      case Some((ficId, typeOrFile)) =>
        val (ficType, filename) = typeOrFile match {
          case t: FicType => (t, None)
          case f: String  => (FicType.Work, Some(f))
        }
        for {
          log    <- if (needToLog) LogMessage.create("Working on it...", bot, userId) else ZIO.succeed(EmptyLog())
          result <- ficsService.getFic(UserFicKey(userId, ficId, ficType), log, filename)
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
      stats         <- StatsService.getGeneralStats(userId)
    } yield HomePageData(
      currentlyReading = started,
      randomFicFromBacklog = randomFic,
      generalStats = stats
    )
  }

  val updateAo3Info = FicsClient.updateAo3Info.implement { (key, needToLog) =>
    for {
      log    <- if (needToLog) LogMessage.create("Working on it...", bot, key.userId) else ZIO.succeed(EmptyLog())
      result <- ao3InfoService.updateAo3Info(key.ficId, key.ficType, log)
      _      <- log.delete
    } yield result
  }

  val toggleParser = FicsClient.toggleParser.implement { _ => ao3InfoService.toggleParser().map(use => s"Now useParser is $use") }

  override val routes: List[Route[Any, Response]] =
    List(getAllCards, getFicByLink, getFicByKey, getHomePage, updateAo3Info, toggleParser)
