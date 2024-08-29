package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.Fandom
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.{FandomDoc, FicsToFandomsDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{FandomsTable, FicsToFandomsTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class FandomsRepo(userId: String) extends WithDb(userId):
  private val allFandoms    = TableQuery[FandomsTable]
  private val ficsToFandoms = TableQuery[FicsToFandomsTable]

  def addAndLink(fandoms: List[Fandom], ficId: String): IO[Throwable, Unit] = for {
    docs <- ZIO.succeed(fandoms.map(FandomDoc.fromModel))
    _    <- add(docs)
    _    <- db(ficsToFandoms ++= docs.map(c => FicsToFandomsDoc(None, ficId, c.fullName)))
  } yield ()

  def getAll: IO[Throwable, List[FandomDoc]] = db(allFandoms.result).map(_.toList)

  def getFicFandoms(ficId: String): IO[Throwable, List[Fandom]] = db((for {
    doc    <- ficsToFandoms if doc.ficId === ficId
    fandom <- allFandoms if fandom.fullName === doc.fandom
  } yield fandom).result).map(_.map(doc => doc.toModel).toList)

  private def add(fandoms: List[FandomDoc]) = db(DBIO.sequence(fandoms.map(addQuery)))

  private def addQuery(fandom: FandomDoc) = fandom.label match
    case Some(label) =>
      sqlu"INSERT OR IGNORE INTO Fandoms (fullName, name, label) VALUES (${fandom.fullName}, ${fandom.name}, $label)"
    case None => sqlu"INSERT OR IGNORE INTO Fandoms (fullName, name) VALUES (${fandom.fullName}, ${fandom.name})"
