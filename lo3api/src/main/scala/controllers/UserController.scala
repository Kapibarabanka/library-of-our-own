package kapibarabanka.lo3.api
package controllers

import kapibarabanka.lo3.common.models.ao3.FicType
import kapibarabanka.lo3.common.models.domain.{UserFicKey, UserFicRecord}
import kapibarabanka.lo3.common.openapi.UserClient
import kapibarabanka.lo3.common.services.{MyBotApi, OptionalLog}
import zio.*
import zio.http.*
import zio.json.*

protected[api] case class UserController(client: Client, bot: MyBotApi) extends Controller:
  private val add = UserClient.add.implement { (id, username) => data.users.addUser(id, username) }

  private val allIds = UserClient.allIds.implement { Unit => data.users.getAllIds }

  private val setEmail = UserClient.setEmail.implement { (id, email) => data.users.setKindleEmail(id, email) }
  private val getEmail = UserClient.getEmail.implement { id => data.users.getKindleEmail(id) }

  private val backlog = UserClient.backlog.implement { (userId, needToLog) =>
    (for {
      log     <- OptionalLog.create("Retrieving backlog...", bot, userId, needToLog)
      keys    <- data.details.getUserBacklog(userId)
      records <- ZIO.collectAll(keys.map(getUserFicInternal))
      _       <- log.edit("Generating HTML...")
      response <- client
        .request(
          Request.post(AppConfig.htmlApi, Body.fromString(BacklogRequest.fromRecords(records).toJson))
        )
        .mapError(e => e.getMessage)
      result <- response.body.asString.mapError(e => e.getMessage)
      _      <- log.delete
    } yield result).provide(Scope.default)
  }

  private def getUserFicInternal(key: UserFicKey): IO[String, UserFicRecord] = for {
    maybeFic <- key.ficType match
      case FicType.Work   => data.works.getById(key.ficId)
      case FicType.Series => data.series.getById(key.ficId)
    fic <- maybeFic match
      case Some(fic) => ZIO.succeed(fic)
      case None      => ZIO.fail("")
    details       <- data.details.getOrCreateDetails(key)
    readDatesInfo <- data.readDates.getReadDatesInfo(key)
    comments      <- data.comments.getAllComments(key)
  } yield UserFicRecord(
    userId = key.userId,
    fic = fic,
    readDatesInfo = readDatesInfo,
    comments = comments,
    details = details
  )

  override val routes: List[Route[Any, Response]] = List(add, allIds, setEmail, getEmail, backlog)
