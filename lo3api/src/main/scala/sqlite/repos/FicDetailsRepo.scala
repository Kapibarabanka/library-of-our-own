package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.FicDetailsDoc
import sqlite.services.Lo3Db
import sqlite.tables.FicsDetailsTable

import kapibarabanka.lo3.common.models.domain.{FicDetails, UserFicKey}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import java.time.LocalDate

class FicDetailsRepo(db: Lo3Db):
  private val ficsDetails = TableQuery[FicsDetailsTable]

  def getUserBacklog(userId: String): IO[String, List[UserFicKey]] = for {
    docs <- db.run(ficsDetails.filter(doc => doc.userId === userId && doc.backlog === true).result)
  } yield docs.map(doc => UserFicKey.fromBool(doc.userId, doc.ficId, doc.ficIsSeries)).toList

  def setBacklog(key: UserFicKey, value: Boolean): IO[String, Unit] =
    db.run(filterDetails(key).map(d => d.backlog).update(value)).unit

  def setOnKindle(key: UserFicKey, value: Boolean): IO[String, Unit] =
    db.run(filterDetails(key).map(d => d.isOnKindle).update(value)).unit

  def addUserFicRecord(key: UserFicKey): IO[String, FicDetails] = for {
    _ <- db.run(
      ficsDetails += FicDetailsDoc(
        id = None,
        userId = key.userId,
        ficId = key.ficId,
        ficIsSeries = key.ficIsSeries,
        backlog = false,
        isOnKindle = false,
        quality = None,
        fire = false,
        recordCreated = LocalDate.now().toString
      )
    )
    maybeDetails <- getDetailsOption(key)
  } yield maybeDetails.get

  def getDetailsOption(key: UserFicKey): IO[String, Option[FicDetails]] = for {
    docs <- db.run(filterDetails(key).result)
  } yield docs.headOption.map(_.toModel)

  def getOrCreateDetails(key: UserFicKey): IO[String, FicDetails] = for {
    maybeDetails <- getDetailsOption(key)
    details <- maybeDetails match
      case Some(value) => ZIO.succeed(value)
      case None        => addUserFicRecord(key)
  } yield details

  // TODO: when marking series as read or on kindle mark all the works with same values
  def patchDetails(key: UserFicKey, details: FicDetails) = for {
    _ <- db.run(
      filterDetails(key)
        .map(d => (d.backlog, d.isOnKindle, d.quality, d.fire))
        .update(
          (
            details.backlog,
            details.isOnKindle,
            details.quality.map(_.toString),
            details.fire
          )
        )
    )
  } yield ()

  private def filterDetails(key: UserFicKey) =
    ficsDetails.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)
