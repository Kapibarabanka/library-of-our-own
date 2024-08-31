package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models.Fandom
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel, MyFicModel, MyFicStats}
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

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
  private val comments          = TableQuery[CommentsTable]

  def addFic(fic: MyFicModel): IO[Throwable, Unit] = {
    val fandomDocs          = fic.fandoms.map(FandomDoc.fromModel)
    val tagDocs             = fic.tags.map(TagDoc.fromModel)
    val shipsWithCharacters = fic.relationships.map(r => (RelationshipDoc.fromModel(r), r.characters.map(CharacterDoc.fromModel)))
    val relationshipDocs    = shipsWithCharacters.map((r, _) => r)
    val characterDocs       = fic.characters.map(CharacterDoc.fromModel) ++ shipsWithCharacters.flatMap((_, c) => c)
    db(
      DBIO.seq(
        fics += FicDoc.fromModel(fic),
        addTags(tagDocs),
        ficsToTags ++= fic.tags.map(tag => FicsToTagsDoc(None, fic.id, tag.name)),
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

  def getFic(ficId: String): IO[Throwable, Option[FicDisplayModel]] = for {
    docs <- db(fics.filter(_.id === ficId).result)
    flatFic <- docs.headOption match
      case Some(doc) => docToFlatModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield flatFic

  def patchStats(ficId: String, stats: MyFicStats): IO[Throwable, FicDisplayModel] = {
    val q = fics.filter(_.id === ficId).map(f => (f.read, f.backlog, f.isOnKindle, f.readDates, f.quality, f.fire))
    val updateAction =
      q.update((stats.read, stats.backlog, stats.isOnKindle, stats.readDates, stats.quality.map(_.toString), stats.fire))
    for {
      _   <- db(updateAction)
      fic <- getFic(ficId).map(_.get)
    } yield fic
  }

  def addComment(ficId: String, comment: FicComment): IO[Throwable, Unit] = db(
    comments += CommentDoc(None, ficId, comment.commentDate, comment.comment)
  ).unit

  private def docToFlatModel(doc: FicDoc) = {
    for {
      fandoms       <- db(ficsToFandoms.filter(_.ficId === doc.id).map(_.fandom).result)
      characters    <- db(ficsToCharacters.filter(_.ficId === doc.id).map(_.character).result)
      relationships <- db(ficsToShips.filter(_.ficId === doc.id).map(_.shipName).result)
      tags          <- db(ficsToTags.filter(_.ficId === doc.id).map(_.tagName).result)
      comments      <- db(comments.filter(_.ficId === doc.id).result)
    } yield doc.toDisplayModel(
      fandoms = fandoms,
      characters = characters,
      relationships = relationships,
      tags = tags,
      comments = comments.map(_.toModel)
    )
  }

  private def addTags(tags: Iterable[TagDoc]) =
    val values = tags.map(t => s"(\"${t.name}\", NULL, ${t.filterable})").mkString(", ")
    sqlu"INSERT OR IGNORE INTO #${TagsTable.name} (name, category, filterable) VALUES #$values"

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
