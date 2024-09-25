package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.{Fandom, FicType, Rating, Work}
import com.kapibarabanka.kapibarabot.domain.{FicDetails, FlatFicModel, Quality}
import com.kapibarabanka.kapibarabot.sqlite.SqliteOld
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import scalaz.Scalaz.ToIdOps
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import scala.collection.immutable.Iterable

class WorksRepo:
  private val works             = TableQuery[WorksTable]
  private val tags              = TableQuery[TagsTable]
  private val worksToTags       = TableQuery[WorksToTagsTable]
  private val fandoms           = TableQuery[FandomsTable]
  private val worksToFandoms    = TableQuery[WorksToFandomsTable]
  private val characters        = TableQuery[CharactersTable]
  private val worksToCharacters = TableQuery[WorksToCharactersTable]
  private val relationships     = TableQuery[RelationshipsTable]
  private val shipsToCharacters = TableQuery[ShipsToCharactersTable]
  private val worksToShips      = TableQuery[WorksToShipsTable]

  def add(work: Work): IO[Throwable, FlatFicModel] =
    SqliteOld.run(DBIO.sequence(getAddingAction(work)).transactionally).flatMap(_ => getById(work.id).map(_.get))

  def getAddingAction(work: Work): List[DBIOAction[Any, NoStream, Effect.Write & Effect.Read]] = {
    val fandomDocs = work.fandoms.map(FandomDoc.fromModel)
    val tagDocs    = work.freeformTags.map(TagDoc.fromModel)
    val shipsWithCharacters =
      work.relationships.map(r => (RelationshipDoc.fromModel(r), r.characters.map(CharacterDoc.fromModel)))
    val relationshipDocs = shipsWithCharacters.map((r, _) => r)
    val characterDocs    = work.characters.map(CharacterDoc.fromModel) ++ shipsWithCharacters.flatMap((_, c) => c)
    List(
      works += WorkDoc.fromModel(work),
      addTags(tagDocs),
      worksToTags ++= work.freeformTags.map(tag => WorksToTagsDoc(None, work.id, tag.name)),
      addFandoms(fandomDocs),
      worksToFandoms ++= fandomDocs.map(f => WorksToFandomsDoc(None, work.id, f.fullName)),
      addCharacters(characterDocs),
      worksToCharacters ++= characterDocs.map(c => WorksToCharactersDoc(None, work.id, c.fullName)),
      addRelationships(relationshipDocs),
      shipsToCharacters ++= shipsWithCharacters.flatMap((ship, characters) =>
        characters.map(c => ShipsToCharactersDoc(None, ship.name, c.fullName))
      ),
      worksToShips ++= relationshipDocs.map(r => WorksToShipsDoc(None, work.id, r.name))
    )
  }

  def getById(workId: String): IO[Throwable, Option[FlatFicModel]] = for {
    docs <- SqliteOld.run(works.filter(_.id === workId).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  def getAll: IO[Throwable, List[FlatFicModel]] = for {
    docs   <- SqliteOld.run(works.result)
    models <- ZIO.collectAll(docs.map(docToModel))
  } yield models.toList

  private def docToModel(doc: WorkDoc) = for {
    fandoms       <- SqliteOld.run(worksToFandoms.filter(_.workId === doc.id).map(_.fandom).result)
    characters    <- SqliteOld.run(worksToCharacters.filter(_.workId === doc.id).map(_.character).result)
    relationships <- SqliteOld.run(worksToShips.filter(_.workId === doc.id).map(_.shipName).result)
    tags          <- SqliteOld.run(worksToTags.filter(_.workId === doc.id).map(_.tagName).result)
  } yield FlatFicModel(
    id = doc.id,
    ficType = FicType.Work,
    link = doc.link,
    title = doc.title,
    authors = doc.authors.split(", ").toList,
    rating = Rating.withName(doc.rating),
    categories = doc.categories.split(", ").toSet,
    fandoms = fandoms.toSet,
    characters = characters.toSet,
    relationships = relationships.toList,
    tags = tags.toList,
    words = doc.words,
    complete = doc.complete
  )

  private def addTags(tags: Iterable[TagDoc]) =
    if (tags.isEmpty) DBIO.successful({})
    else
      val values = tags.map(t => s"(\"${t.name |> formatForSql}\", NULL, ${t.filterable})").mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${TagsTable.name} (name, category, filterable) VALUES #$values"

  private def addFandoms(fandoms: Iterable[FandomDoc]) =
    if (fandoms.isEmpty) Query.empty.result
    else
      val values = fandoms
        .map(f =>
          s"(\"${f.fullName |> formatForSql}\", \"${f.name}\", ${f.label.fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
        )
        .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${FandomsTable.name} (fullName, name, label) VALUES #$values"

  private def addCharacters(characters: Iterable[CharacterDoc]) =
    if (characters.isEmpty) Query.empty.result
    else
      val values =
        characters
          .map(c =>
            s"(\"${c.fullName |> formatForSql}\", \"${c.name |> formatForSql}\", ${c.label
                .fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
          )
          .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${CharactersTable.name} (fullName, name, label) VALUES #$values"

  private def addRelationships(ships: Iterable[RelationshipDoc]) =
    if (ships.isEmpty) Query.empty.result
    else
      val values =
        ships
          .map(r =>
            s"(\"${r.name |> formatForSql}\", \"${r.relationshipType}\", ${r.nameInFic.fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
          )
          .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${RelationshipsTable.name} (name, relationshipType, nameInFic) VALUES #$values"

  private def formatForSql(str: String) = str.replace("\"", "\"\"")
