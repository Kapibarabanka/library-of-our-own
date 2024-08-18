package com.kapibarabanka.kapibarabot.persistence

import cats.implicits.*
import com.kapibarabanka.airtable.{AirtableError, EntityDocument, Record, Table}
import com.kapibarabanka.ao3scrapper.Ao3Url
import com.kapibarabanka.ao3scrapper.models.{Fic, FicType, Relationship}
import com.kapibarabanka.kapibarabot.domain.{MyFicRecord, MyFicStats}
import com.kapibarabanka.kapibarabot.persistence.Mapper.toAirtable
import com.kapibarabanka.kapibarabot.persistence.tables.*
import com.kapibarabanka.kapibarabot.persistence.docs.*
import org.http4s.client.Client
import zio.*

import scala.collection.parallel.CollectionConverters.*

class AirtableClient(http: Client[Task], authToken: String) {
  val fics: FicsTable                   = FicsTable(authToken, http)
  val tags: FreeformTable               = FreeformTable(authToken, http)
  val characters: CharactersTable       = CharactersTable(authToken, http)
  val fandoms: FandomsTable             = FandomsTable(authToken, http)
  val relationships: RelationshipsTable = RelationshipsTable(authToken, http)
  val stats: StatsTable                 = StatsTable(authToken, http)

  def addFic(fic: Fic): IO[AirtableError, MyFicRecord] = for {
    doc      <- convert(fic)
    record   <- fics.upsert(doc)
    myRecord <- getMyRecord(record)
  } yield myRecord

  def patchFicStats(airtableId: String, myStats: MyFicStats): IO[AirtableError, MyFicRecord] = for {
    statsDoc <- ZIO.succeed(Mapper.toStatsDoc(myStats))
    record   <- stats.patchFicStats(airtableId, statsDoc)
    myRecord <- getMyRecord(record)
  } yield myRecord

  def getFicByLink(link: String): IO[AirtableError, Option[MyFicRecord]] = for {
    recordOption <- getFicRecordByLink(link)
    myRecord <- recordOption match
      case Some(record) => getMyRecord(record).map(Some(_))
      case None         => ZIO.succeed(None)
  } yield myRecord

  def deleteFic(airtableId: String): IO[AirtableError, Option[String]] = for {
    ficRecord <- fics.find(airtableId)
    _         <- deleteIfSingleWork(ficRecord.fields.Relationships, relationships)
    _         <- deleteIfSingleWork(ficRecord.fields.Fandoms, fandoms)
    _         <- deleteIfSingleWork(ficRecord.fields.Characters, characters)
    _         <- deleteIfSingleWork(ficRecord.fields.Tags, tags)
    _         <- fics.delete(ficRecord.id.get)
  } yield ficRecord.id

  def deleteLonelyTags(): IO[AirtableError, Unit] = for {
    allTags    <- tags.getAll
    lonelyTags <- ZIO.succeed(allTags.filter(r => r.fields.WorkCount.getOrElse(0) <= 1))
    _          <- tags.delete(lonelyTags.map(r => r.id.get))
  } yield ()

  def collectFilterable(): IO[AirtableError, Unit] = for {
    allTags <- tags.getAll
    tagMap  <- ZIO.succeed(allTags.map(r => r.id.get -> r.fields.Name).toMap)
    allFics <- fics.getAll
    patchedFics <- ZIO.succeed(
      allFics.map(r => r.copy(fields = r.fields.copy(FilterableTags = Some(r.fields.Tags.map(id => tagMap(id)).mkString(", ")))))
    )
    _ <- fics.upsert(patchedFics.map(r => r.fields))
  } yield ()

  def collectCharacters(): IO[AirtableError, Unit] = for {
    allCharacters <- characters.getAll
    characterMap  <- ZIO.succeed(allCharacters.map(r => r.id.get -> r.fields.Name).toMap)
    allFics       <- fics.getAll
    patchedFics <- ZIO.succeed(
      allFics.map(r =>
        r.copy(fields = r.fields.copy(AllCharacters = Some(r.fields.Characters.map(id => characterMap(id)).mkString(", "))))
      )
    )
    _ <- fics.upsert(patchedFics.map(r => r.fields))
  } yield ()

  private def getFicRecordByLink(link: String): IO[AirtableError, Option[Record[FicDocument]]] =
    Ao3Url.tryParseFicId(link) match
      case Some((_, id)) => fics.findByAo3Id(id)
      case _             => ZIO.succeed(None)

  private def convert(fic: Fic): IO[AirtableError, FicDocument] = {
    for {
      fandomRecords <- fandoms.upsert(fic.fandoms.map(f => TagDocument(f.name)).toList)
      characterRecords <- characters.upsert(
        fic.characters
          .union(fic.relationships.flatMap(s => s.characters).toSet)
          .map(c => TagDocument(c.name))
          .toList
      )
      freeformTagRecords <-
        tags.upsert(
          fic.freeformTags
            .filter(t => t.isFilterable.getOrElse(false))
            .map(t => TagDocument(t.name))
        )
      relationsRecords <- upsertShips(fic.relationships, characterRecords)
    } yield toAirtable(
      fic,
      fandomRecords.map(r => r.id.get),
      relationsRecords.map(r => r.id.get),
      characterRecords.map(r => r.id.get),
      freeformTagRecords.map(r => r.id.get)
    )
  }

  private def deleteIfSingleWork[A <: EntityDocument & WithWorkCount](ids: List[String], table: Table[A]) = {
    for {
      docs <- ZIO.collectAll(ids.map(table.find))
      _    <- ZIO.collectAll(docs.filter(r => r.fields.WorkCount.getOrElse(0) <= 1).map(r => table.delete(r.id.get)))
    } yield ()
  }

  private def upsertShips(ships: List[Relationship], characters: List[Record[TagDocument]]) = {
    val characterNameToId = characters.map(r => r.fields.Name -> r.id.get).toMap
    val documents = ships.map(s =>
      RelationshipDocument(
        s.nameInFic.getOrElse(s.name),
        s.characters.flatMap(c => characterNameToId.get(c.name)).toList,
        s.shipType.toString
      )
    )
    relationships.upsert(documents)
  }

  private def getMyRecord(airtableRecord: Record[FicDocument]) = for {
    ships      <- ZIO.collectAll(airtableRecord.fields.Relationships.par.map(id => relationships.find(id)).toList)
    fandoms    <- ZIO.collectAll(airtableRecord.fields.Fandoms.par.map(id => fandoms.find(id)).toList)
    characters <- ZIO.collectAll(airtableRecord.fields.Characters.par.map(id => characters.find(id)).toList)
  } yield Mapper.toMyRecord(airtableRecord, ships, fandoms, characters)
}
