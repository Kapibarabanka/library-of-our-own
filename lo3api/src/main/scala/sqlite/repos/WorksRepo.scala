package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.*
import sqlite.services.Lo3Db
import sqlite.tables.*

import kapibarabanka.lo3.common.models.ao3.{Fandom, FicType, Rating, Work}
import kapibarabanka.lo3.common.models.domain.{DbError, FicDetails, Ao3FicInfo}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class WorksRepo(db: Lo3Db, tagsRepo: TagsRepo):
  private val works             = TableQuery[WorksTable]
  private val worksToTags       = TableQuery[WorksToTagsTable]
  private val worksToFandoms    = TableQuery[WorksToFandomsTable]
  private val worksToCharacters = TableQuery[WorksToCharactersTable]
  private val shipsToCharacters = TableQuery[ShipsToCharactersTable]
  private val worksToShips      = TableQuery[WorksToShipsTable]
  private val ficDetails        = TableQuery[FicsDetailsTable]

  def exists(id: String): IO[DbError, Boolean] =
    db.run(works.filter(_.id === id).result).map(docs => docs.headOption.nonEmpty)

  def title(id: String) = db.run(works.filter(_.id === id).map(_.title).result).map(_.headOption)

  def add(work: Work): IO[DbError, Ao3FicInfo] =
    db.run(DBIO.sequence(getAddingAction(work, true)).transactionally).flatMap(_ => getById(work.id).map(_.get))

  def updateWork(work: Work): IO[DbError, Ao3FicInfo] = for {
    maybeExisting <- getById(work.id)
    updated <- maybeExisting match
      case None => add(work)
      case Some(existing) =>
        for {
          newTagsOnly <- ZIO.succeed(
            work.copy(
              fandoms = work.fandoms.filterNot(f => existing.fandoms.contains(f.fullName)),
              freeformTags = work.freeformTags.filterNot(t => existing.tags.contains(t.name)),
              characters = work.characters.filterNot(c => existing.characters.contains(c.fullName)),
              relationships = work.relationships.filterNot(r => existing.relationships.contains(r.fullName))
            )
          )
          updatedFic <- db
            .run(
              DBIO
                .sequence(
                  Seq(works.filter(_.id === work.id).update(WorkDoc.fromModel(newTagsOnly))) ++ getAddingAction(
                    newTagsOnly,
                    false
                  )
                )
                .transactionally
            )
            .flatMap(_ => getById(work.id).map(_.get))
        } yield updatedFic
  } yield updated

  def getAddingAction(work: Work, insertWork: Boolean): List[DBIOAction[Any, NoStream, Effect.Write & Effect.Read]] = {
    val fandomDocs = work.fandoms.map(FandomDoc.fromModel)
    val tagDocs    = work.freeformTags.distinct.map(TagDoc.fromModel)
    val shipsWithCharacters =
      work.relationships.distinct.map(r => (RelationshipDoc.fromModel(r), r.characters.map(CharacterDoc.fromModel)))
    val relationshipDocs = shipsWithCharacters.map((r, _) => r)
    val characterDocs    = work.characters.map(CharacterDoc.fromModel) ++ shipsWithCharacters.flatMap((_, c) => c)
    val initial          = if (insertWork) List(works += WorkDoc.fromModel(work)) else List()
    val result = initial ++ List(
      tagsRepo.addTags(tagDocs),
      worksToTags ++= work.freeformTags.map(tag => WorksToTagsDoc(None, work.id, tag.name)),
      tagsRepo.addFandoms(fandomDocs),
      worksToFandoms ++= fandomDocs.map(f => WorksToFandomsDoc(None, work.id, f.fullName)),
      tagsRepo.addCharacters(characterDocs),
      worksToCharacters ++= characterDocs.map(c => WorksToCharactersDoc(None, work.id, c.fullName)),
      tagsRepo.addRelationships(relationshipDocs),
      shipsToCharacters ++= shipsWithCharacters.flatMap((ship, characters) =>
        characters.map(c => ShipsToCharactersDoc(None, ship.name, c.fullName))
      ),
      worksToShips ++= relationshipDocs.map(r => WorksToShipsDoc(None, work.id, r.name)),
      worksToShips ++= work.parsedShips.map(name => WorksToShipsDoc(None, work.id, name))
    )
    result
  }

  def getById(workId: String): IO[DbError, Option[Ao3FicInfo]] = for {
    docs <- db.run(works.filter(_.id === workId).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  private def docToModel(doc: WorkDoc) = for {
    fandoms       <- db.run(worksToFandoms.filter(_.workId === doc.id).map(_.fandom).result)
    characters    <- db.run(worksToCharacters.filter(_.workId === doc.id).map(_.character).result)
    relationships <- db.run(worksToShips.filter(_.workId === doc.id).map(_.shipName).result)
    tags          <- db.run(worksToTags.filter(_.workId === doc.id).map(_.tagName).result)
  } yield Ao3FicInfo(
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
    relationships = relationships.distinct.toList,
    tags = tags.toList.distinct,
    words = doc.words,
    complete = doc.complete,
    partsWritten = doc.partsWritten,
    downloadLink = doc.downloadLink
  )
