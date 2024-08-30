package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models.Fandom
import com.kapibarabanka.kapibarabot.domain.MyFicModel
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import slick.jdbc.PostgresProfile.api.*
import zio.IO

import scala.collection.immutable.Iterable

class FicsRepo(userId: String) extends WithDb(userId):
  private val fics              = TableQuery[FicsTable]
  private val tags              = TableQuery[TagsTable]
  private val ficsToTags        = TableQuery[FicsToTagsTable]
  private val fandoms           = TableQuery[FandomsTable]
  private val ficsToFandoms     = TableQuery[FicsToFandomsTable]
  private val characters        = TableQuery[CharactersTable]
  private val ficsToCharacters  = TableQuery[FicsToCharactersTable]
  private val relationships     = TableQuery[RelationshipsTable]
  private val shipsToCharacters = TableQuery[ShipsToCharactersTable]
  private val ficsToShips       = TableQuery[FicsToShipsTable]

  def addFic(fic: MyFicModel): IO[Throwable, Unit] = {
    val fandomDocs          = fic.fandoms.map(FandomDoc.fromModel)
    val shipsWithCharacters = fic.relationships.map(r => (RelationshipDoc.fromModel(r), r.characters.map(CharacterDoc.fromModel)))
    val relationshipDocs    = shipsWithCharacters.map((r, _) => r)
    val characterDocs       = fic.characters.map(CharacterDoc.fromModel) ++ shipsWithCharacters.flatMap((_, c) => c)
    db(
      DBIO.seq(
        fics += FicDoc.fromModel(fic),
        addTags(fic.tags),
        ficsToTags ++= fic.tags.map(tag => FicsToTagsDoc(None, fic.id, tag)),
        addFandoms(fandomDocs),
        ficsToFandoms ++= fandomDocs.map(f => FicsToFandomsDoc(None, fic.id, f.fullName)),
        addCharacters(characterDocs),
        ficsToCharacters ++= characterDocs.map(c => FicsToCharactersDoc(None, fic.id, c.fullName)),
        addRelationships(relationshipDocs),
        shipsToCharacters ++= shipsWithCharacters.flatMap((ship, characters) =>
          characters.map(c => ShipsToCharactersDoc(None, ship.name, c.fullName))
        ),
        ficsToShips ++= relationshipDocs.map(r => FicsToShipsDoc(None, fic.id, r.name))
      )
    )
  }

  private def addTags(tags: Iterable[String]) =
    val values = tags.map(t => s"(\"$t\", NULL)").mkString(", ")
    sqlu"INSERT OR IGNORE INTO #${TagsTable.name} (name, category) VALUES #$values"

  private def addFandoms(fandoms: Iterable[FandomDoc]) =
    val values = fandoms.map(f => s"(\"${f.fullName}\", \"${f.name}\", ${f.label.fold("NULL")(l => s"\"$l\"")})").mkString(", ")
    sqlu"INSERT OR IGNORE INTO #${FandomsTable.name} (fullName, name, label) VALUES #$values"

  private def addCharacters(characters: Iterable[CharacterDoc]) =
    val values =
      characters.map(c => s"(\"${c.fullName}\", \"${c.name}\", ${c.label.fold("NULL")(l => s"\"$l\"")})").mkString(", ")
    sqlu"INSERT OR IGNORE INTO #${CharactersTable.name} (fullName, name, label) VALUES #$values"

  private def addRelationships(ships: Iterable[RelationshipDoc]) =
    val values =
      ships.map(r => s"(\"${r.name}\", \"${r.relationshipType}\", ${r.nameInFic.fold("NULL")(l => s"\"$l\"")})").mkString(", ")
    sqlu"INSERT OR IGNORE INTO #${RelationshipsTable.name} (name, relationshipType, nameInFic) VALUES #$values"
