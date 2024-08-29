package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.Character
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.{CharacterDoc, FicsToCharactersDoc, ShipsToCharactersDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{CharactersTable, FicsToCharactersTable, ShipsToCharactersTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class CharactersRepo(userId: String) extends WithDb(userId):
  private val allCharacters     = TableQuery[CharactersTable]
  private val ficsToCharacters  = TableQuery[FicsToCharactersTable]
  private val shipsToCharacters = TableQuery[ShipsToCharactersTable]

  def addAndLinkToFic(characters: List[Character], ficId: String): IO[Throwable, Unit] = for {
    docs <- ZIO.succeed(characters.map(CharacterDoc.fromModel))
    _    <- add(docs)
    _    <- db(ficsToCharacters ++= docs.map(c => FicsToCharactersDoc(None, ficId, c.fullName)))
  } yield ()

  def addAndLinkToShip(characters: List[Character], shipName: String): IO[Throwable, Unit] = for {
    docs <- ZIO.succeed(characters.map(CharacterDoc.fromModel))
    _    <- add(docs)
    _    <- db(shipsToCharacters ++= docs.map(c => ShipsToCharactersDoc(None, shipName, c.fullName)))
  } yield ()

  def getAll: IO[Throwable, List[CharacterDoc]] = db(allCharacters.result).map(_.toList)

  def getFicCharacters(ficId: String): IO[Throwable, List[Character]] = db((for {
    doc       <- ficsToCharacters if doc.ficId === ficId
    character <- allCharacters if character.fullName === doc.character
  } yield character).result).map(characterDocs => characterDocs.map(doc => doc.toModel).toList)

  def getShipCharacters(shipName: String): IO[Throwable, List[Character]] = db((for {
    doc       <- shipsToCharacters if doc.shipName === shipName
    character <- allCharacters if character.fullName === doc.character
  } yield character).result).map(_.map(doc => doc.toModel).toList)

  private def add(characters: List[CharacterDoc]) = db(DBIO.sequence(characters.map(addQuery)))

  private def addQuery(character: CharacterDoc) = character.label match
    case Some(label) =>
      sqlu"INSERT OR IGNORE INTO Characters (fullName, name, label) VALUES (${character.fullName}, ${character.name}, $label)"
    case None => sqlu"INSERT OR IGNORE INTO Characters (fullName, name) VALUES (${character.fullName}, ${character.name})"
