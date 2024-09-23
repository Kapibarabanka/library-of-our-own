package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.{Fandom, FicType, Rating, Work}
import com.kapibarabanka.kapibarabot.domain.{FicDisplayModel, MyFicStats, Quality}
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import scalaz.Scalaz.ToIdOps
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.FixedSqlAction
import zio.{IO, ZIO}

import scala.collection.immutable.Iterable

class WorksRepo(userId: String) extends WithDb(userId):
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
  private val stats             = StatsRepo(userId)

  def add(work: Work): IO[Throwable, FicDisplayModel] =
    db(DBIO.sequence(getAddingAction(work)).transactionally).flatMap(_ => getById(work.id).map(_.get))

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

  def getById(workId: String): IO[Throwable, Option[FicDisplayModel]] = for {
    docs <- db(works.filter(_.id === workId).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToDisplayModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  def getAll: IO[Throwable, List[FicDisplayModel]] = for {
    docs   <- db(works.result)
    models <- ZIO.collectAll(docs.map(docToDisplayModel))
  } yield models.toList

  def patchStats(workId: String, stats: MyFicStats): IO[Throwable, FicDisplayModel] = {
    val q = works.filter(_.id === workId).map(w => (w.read, w.backlog, w.isOnKindle, w.quality, w.fire))
    val updateAction =
      q.update((stats.read, stats.backlog, stats.isOnKindle, stats.quality.map(_.toString), stats.fire))
    for {
      _   <- db(updateAction)
      fic <- getById(workId).map(_.get)
    } yield fic
  }

  def updateStatsFromSeriesAction(workId: String, seriesStats: MyFicStats): FixedSqlAction[Int, NoStream, Effect.Write] = {
    val q = works.filter(_.id === workId).map(workDoc => (workDoc.read, workDoc.isOnKindle, workDoc.quality))
    q.update((seriesStats.read, seriesStats.isOnKindle, seriesStats.quality.map(_.toString)))
  }

  private def docToDisplayModel(doc: WorkDoc) = for {
    comments      <- stats.getAllComments(doc.key)
    readDatesInfo <- stats.getReadDatesInfo(doc.key)
    fandoms       <- db(worksToFandoms.filter(_.workId === doc.id).map(_.fandom).result)
    characters    <- db(worksToCharacters.filter(_.workId === doc.id).map(_.character).result)
    relationships <- db(worksToShips.filter(_.workId === doc.id).map(_.shipName).result)
    tags          <- db(worksToTags.filter(_.workId === doc.id).map(_.tagName).result)
  } yield FicDisplayModel(
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
    comments = comments,
    readDatesInfo = readDatesInfo,
    words = doc.words,
    complete = doc.complete,
    stats = MyFicStats(
      read = doc.read,
      backlog = doc.backlog,
      isOnKindle = doc.isOnKindle,
      quality = doc.quality.map(Quality.withName),
      fire = doc.fire
    )
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
