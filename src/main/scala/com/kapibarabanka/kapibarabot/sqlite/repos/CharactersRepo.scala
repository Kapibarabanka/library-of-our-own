package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.Character
import com.kapibarabanka.kapibarabot.sqlite.docs.{CharacterDoc, CharactersTable, FicsToCharactersDoc, FicsToCharactersTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class CharactersRepo(userId: String) extends WithDb(userId):
  private val allCharacters    = TableQuery[CharactersTable]
  private val ficsToCharacters = TableQuery[FicsToCharactersTable]

  def addAndLink(characters: List[Character], ficId: String): IO[Throwable, Unit] = for {
    docs <- ZIO.succeed(characters.map(CharacterDoc.fromModel))
    _    <- add(docs)
    _    <- db(ficsToCharacters ++= docs.map(c => FicsToCharactersDoc(None, ficId, c.fullName)))
  } yield ()

  def getAll: IO[Throwable, List[CharacterDoc]] = db(allCharacters.result).map(_.toList)

  def getFicCharacters(ficId: String): IO[Throwable, List[CharacterDoc]] = db((for {
    doc       <- ficsToCharacters if doc.ficId === ficId
    character <- allCharacters if character.fullName === doc.character
  } yield character).result).map(_.toList)

  private def add(characters: List[CharacterDoc]) = db(DBIO.sequence(characters.map(addQuery)))

  private def addQuery(character: CharacterDoc) = character.label match
    case Some(label) =>
      sqlu"INSERT OR IGNORE INTO Characters (fullName, name, label) VALUES (${character.fullName}, ${character.name}, $label)"
    case None => sqlu"INSERT OR IGNORE INTO Characters (fullName, name) VALUES (${character.fullName}, ${character.name})"

  def initIfNotExists = db(
    DBIO.seq(
      allCharacters.schema.createIfNotExists,
      sqlu"""
    CREATE TABLE IF NOT EXISTS "FicsToCharacters" (
	"id"	INTEGER NOT NULL UNIQUE,
	"ficId"	TEXT NOT NULL,
	"character"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("ficId") REFERENCES "Fics"("id") ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY("character") REFERENCES "Characters"("fullName") ON UPDATE CASCADE ON DELETE CASCADE);
    """
    )
  )
