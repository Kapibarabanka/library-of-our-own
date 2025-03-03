package kapibarabanka.lo3.api
package controllers

import sqlite.services.Lo3Data

import kapibarabanka.lo3.common.AppConfig
import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.{Lo3Error, UnspecifiedError, UserFicKey, Fic}
import kapibarabanka.lo3.common.openapi.UserClient
import kapibarabanka.lo3.common.services.{EmptyLog, LogMessage, MyBotApi}
import zio.*
import zio.http.*
import zio.json.*

protected[api] case class UserController(client: Client, bot: MyBotApi) extends Controller:
  private val add = UserClient.add.implement { (id, username) => Lo3Data.users.addUser(id, username) }

  private val allIds = UserClient.allIds.implement { Unit => Lo3Data.users.getAllIds }

  private val setEmail = UserClient.setEmail.implement { (id, email) => Lo3Data.users.setKindleEmail(id, email) }
  private val getEmail = UserClient.getEmail.implement { id => Lo3Data.users.getKindleEmail(id) }

  private val backlog = UserClient.backlog.implement { (userId, needToLog) =>
    (for {
      log     <- if (needToLog) LogMessage.create("Retrieving backlog...", bot, userId) else ZIO.succeed(EmptyLog())
      keys    <- Lo3Data.details.getUserBacklog(userId)
      records <- ZIO.collectAll(keys.map(getUserFicInternal))
      _       <- log.edit("Generating HTML...")
      response <- client
        .request(
          Request.post(AppConfig.htmlApi, Body.fromString(BacklogRequest.fromRecords(records).toJson))
        )
        .mapError(e => UnspecifiedError(e.getMessage))
      result <- response.body.asString.mapError(e => UnspecifiedError(e.getMessage))
      _      <- log.delete
    } yield result).provide(Scope.default)
  }

  private def getUserFicInternal(key: UserFicKey): IO[Lo3Error, Fic] = for {
    maybeFic <- key.ficType match
      case FicType.Work   => Lo3Data.works.getById(key.ficId)
      case FicType.Series => Lo3Data.series.getById(key.ficId)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None      => ZIO.fail(UnspecifiedError("Shouldn't be possible :)"))
    details       <- Lo3Data.details.getOrCreateDetails(key)
    readDatesInfo <- Lo3Data.readDates.getReadDatesInfo(key)
    notes         <- Lo3Data.notes.getAllNotes(key)
  } yield Fic(
    userId = key.userId,
    ao3Info = fic,
    readDatesInfo = readDatesInfo,
    notes = notes,
    details = details
  )

  override val routes: List[Route[Any, Response]] = List(add, allIds, setEmail, getEmail, backlog)
