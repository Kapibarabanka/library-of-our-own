package kapibarabanka.lo3.api
package controllers

import services.FicsService
import services.ao3Info.Ao3InfoService
import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.models.ao3.Ao3Url
import kapibarabanka.lo3.common.models.domain.{FicCard, NotAo3Link, UserFicKey}
import kapibarabanka.lo3.common.openapi.FicsClient
import kapibarabanka.lo3.common.services.{EmptyLog, LogMessage, MyBotApi}
import zio.ZIO
import zio.http.{Response, Route}

import java.time.LocalDateTime

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

  val updateAo3Info = FicsClient.updateAo3Info.implement { key =>
    for {
      log    <- if (key.userId.nonEmpty) LogMessage.create("Working on it...", bot, key.userId) else ZIO.succeed(EmptyLog())
      result <- ao3InfoService.updateAo3Info(key.ficId, key.ficType, log)
      _      <- log.delete
    } yield result
  }

  override val routes: List[Route[Any, Response]] = List(getAllCards, getFicByLink, getFicByKey, updateAo3Info)
