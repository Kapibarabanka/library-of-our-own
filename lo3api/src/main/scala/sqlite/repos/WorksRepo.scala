package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.*
import sqlite.services.Lo3Db
import sqlite.tables.*

import kapibarabanka.lo3.common.models.ao3.{Fandom, FicType, Rating, Work}
import kapibarabanka.lo3.common.models.api.FicCard
import kapibarabanka.lo3.common.models.domain.{DbError, FicDetails, FlatFicModel}
import scalaz.Scalaz.ToIdOps
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import scala.collection.immutable.Iterable

class WorksRepo(db: Lo3Db):
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
  private val ficDetails        = TableQuery[FicsDetailsTable]

  def exists(id: String): IO[DbError, Boolean] =
    db.run(works.filter(_.id === id).result).map(docs => docs.headOption.nonEmpty)

  def title(id: String) = db.run(works.filter(_.id === id).map(_.title).result).map(_.headOption)

  def add(work: Work): IO[DbError, FlatFicModel] =
    db.run(DBIO.sequence(getAddingAction(work)).transactionally).flatMap(_ => getById(work.id).map(_.get))

  def getAddingAction(work: Work): List[DBIOAction[Any, NoStream, Effect.Write & Effect.Read]] = {
    val fandomDocs = work.fandoms.map(FandomDoc.fromModel)
    val tagDocs    = work.freeformTags.distinct.map(TagDoc.fromModel)
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

  def getById(workId: String): IO[DbError, Option[FlatFicModel]] = for {
    docs <- db.run(works.filter(_.id === workId).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  def getAllForUser(userId: String): IO[DbError, List[(FicDetails, FlatFicModel)]] =
    val detailsQuery = ficDetails.filter(d => d.ficIsSeries === false && d.userId === userId)
    for {
      worksWithDetails <- db.run((for {
        details <- detailsQuery
        work    <- works if (work.id === details.ficId)
      } yield (details, work)).result)
      fandomsByWork <- db
        .run((for {
          details <- detailsQuery
          fandom  <- worksToFandoms if (fandom.workId === details.ficId)
        } yield (details, fandom)).result)
        .map(seq => seq.groupMap((d, _) => d.ficId)((_, f) => f.fandom))
      charactersByWork <- db
        .run((for {
          details   <- detailsQuery
          character <- worksToCharacters if (character.workId === details.ficId)
        } yield (details, character)).result)
        .map(seq => seq.groupMap((d, _) => d.ficId)((_, c) => c.character))
      shipsByWork <- db
        .run((for {
          details   <- detailsQuery
          character <- worksToShips if (character.workId === details.ficId)
        } yield (details, character)).result)
        .map(seq => seq.groupMap((d, _) => d.ficId)((_, s) => s.shipName))
      tagsByWork <- db
        .run((for {
          details <- detailsQuery
          tag     <- worksToTags if (tag.workId === details.ficId)
        } yield (details, tag)).result)
        .map(seq => seq.groupMap((d, _) => d.ficId)((_, t) => t.tagName))
      fics <- ZIO.succeed(
        worksWithDetails.map((details, work) =>
          (
            details.toModel,
            FlatFicModel(
              id = work.id,
              ficType = FicType.Work,
              link = work.link,
              title = work.title,
              authors = work.authors.split(", ").toList,
              rating = Rating.withName(work.rating),
              categories = work.categories.split(", ").toSet,
              warnings = work.warnings.split(", ").toSet,
              fandoms = fandomsByWork.getOrElse(work.id, Seq()).toSet,
              characters = charactersByWork.getOrElse(work.id, Seq()).toSet,
              relationships = shipsByWork.getOrElse(work.id, Seq()).toList,
              tags = tagsByWork.getOrElse(work.id, Seq()).toList.distinct,
              words = work.words,
              complete = work.complete
            )
          )
        )
      )
    } yield fics.toList

  def getAll: IO[DbError, List[FlatFicModel]] = for {
    docs   <- db.run(works.result)
    models <- ZIO.collectAll(docs.map(docToModel))
  } yield models.toList

  private def docToModel(doc: WorkDoc) = for {
    fandoms       <- db.run(worksToFandoms.filter(_.workId === doc.id).map(_.fandom).result)
    characters    <- db.run(worksToCharacters.filter(_.workId === doc.id).map(_.character).result)
    relationships <- db.run(worksToShips.filter(_.workId === doc.id).map(_.shipName).result)
    tags          <- db.run(worksToTags.filter(_.workId === doc.id).map(_.tagName).result)
  } yield FlatFicModel(
    id = doc.id,
    ficType = FicType.Work,
    link = doc.link,
    title = doc.title,
    authors = doc.authors.split(", ").toList,
    rating = Rating.withName(doc.rating),
    categories = doc.categories.split(", ").toSet,
    warnings = doc.warnings.split(", ").toSet,
    fandoms = fandoms.toSet,
    characters = characters.toSet,
    relationships = relationships.toList,
    tags = tags.toList.distinct,
    words = doc.words,
    complete = doc.complete
  )

  private def addTags(tags: Iterable[TagDoc]) =
    if (tags.isEmpty) DBIO.successful({})
    else
      val values = tags.map(t => s"(\"${t.name |> formatForSql}\", NULL, ${t.filterable})").mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${TagsTable.name} (name, category, filterable) VALUES #$values"

  private def addFandoms(fandoms: Iterable[FandomDoc]) =
    if (fandoms.isEmpty) DBIO.successful({})
    else
      val values = fandoms
        .map(f =>
          s"(\"${f.fullName |> formatForSql}\", \"${f.name}\", ${f.label.fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
        )
        .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${FandomsTable.name} (fullName, name, label) VALUES #$values"

  private def addCharacters(characters: Iterable[CharacterDoc]) =
    if (characters.isEmpty) DBIO.successful({})
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
    if (ships.isEmpty) DBIO.successful({})
    else
      val values =
        ships
          .map(r =>
            s"(\"${r.name |> formatForSql}\", \"${r.relationshipType}\", ${r.nameInFic.fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
          )
          .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${RelationshipsTable.name} (name, relationshipType, nameInFic) VALUES #$values"

  private def formatForSql(str: String) = str.replace("\"", "\"\"")
