package com.kapibarabanka.kapibarabot.persistence

import com.kapibarabanka.airtable.Record
import com.kapibarabanka.ao3scrapper.models.*
import com.kapibarabanka.ao3scrapper.{StringUtils, models}
import com.kapibarabanka.kapibarabot.domain.{MyFicRecord, MyFicStats, Quality}
import com.kapibarabanka.kapibarabot.persistence.docs.*
import scalaz.Scalaz.ToIdOps

import scala.language.postfixOps

object Mapper:
  def toAirtable(
      fic: Fic,
      fandoms: List[String],
      ships: List[String],
      characters: List[String],
      tags: List[String]
  ): FicDocument = {
    val (parts, isSeries) = fic match
      case work: Work     => (work.partsWritten, false)
      case series: Series => (series.workIds.size, true)
    FicDocument(
      Name = fic.title,
      Author = fic.authors.mkString(", "),
      Rating = (fic.rating.toString.substring(0, 1)),
      Warnings = fic.warnings.map(_.name).mkString(", "),
      Categories = fic.categories.map(_.toString).toList,
      Fandoms = fandoms,
      Relationships = ships,
      Characters = characters,
      AllCharacters = Some(fic.characters.map(_.name).mkString(", ")),
      Tags = tags,
      AllTags = Some(fic.freeformTags.map(_.name).mkString(", ")),
      FilterableTags = Some(fic.freeformTags.filter(_.isFilterable.getOrElse(false)).map(_.name).mkString(", ")),
      Published = StringUtils.dateFormat.format(fic.started),
      Updated = fic.updated.map(StringUtils.dateFormat.format),
      Words = fic.words,
      ChaptersWritten = parts,
      Completed = fic.complete,
      Link = fic.link,
      ao3Id = fic.id,
      isSeries,
      Read = None,
      Backlog = None,
      IsOnKindle = None,
      ReadDates = None
    )
  }

  def toMyRecord(
      airtable: Record[FicDocument],
      shipRecords: List[Record[RelationshipDocument]],
      fandomRecords: List[Record[TagDocument]],
      characterRecords: List[Record[TagDocument]]
  ): MyFicRecord = {
    val ficDoc            = airtable.fields
    val characterNameToId = characterRecords.map(r => r.id.get -> r.fields.Name).toMap

    val id         = ficDoc.ao3Id
    val title      = ficDoc.Name
    val authors    = ficDoc.Author.split(", ").toList
    val rating     = ficDoc.Rating |> toRating
    val warnings   = ficDoc.Warnings.split(", ").toSet.map(ArchiveWarning(_))
    val categories = ficDoc.Categories.map(Category.withName).toSet
    val fandoms    = fandomRecords.map(r => Fandom(r.fields.Name)).toSet
    val relationships = shipRecords.map(r =>
      Relationship(
        r.fields.Characters.map(id => Character(characterNameToId(id))).toSet,
        RelationshipType.withName(r.fields.Type)
      )
    )
    val characters = characterRecords.map(r => Character(r.fields.Name)).toSet
    val freeformTags = ficDoc.AllTags match
      // TODO: pass tags from airtable, if tag is there it's filterable
      case Some(tags) => tags.split(", ").toList.map(FreeformTag(_, None))
      case None       => List()
    val link    = ficDoc.Link
    val started = ficDoc.Published |> StringUtils.parseDate
    val updated = ficDoc.Updated.map(StringUtils.parseDate)
    val words   = ficDoc.Words

    val fic =
      if ficDoc.IsSeries then
        Series(
          id,
          title,
          authors,
          rating,
          warnings,
          categories,
          fandoms,
          relationships,
          characters,
          freeformTags,
          link,
          started,
          updated,
          words,
          // TODO: maybe add this info to airtable, maybe not
          bookmarks = None,
          complete = ficDoc.Completed,
          workIds = Range.inclusive(1, ficDoc.ChaptersWritten).toList.map(_.toString),
          description = None
        )
      else
        Work(
          id,
          title,
          authors,
          rating,
          warnings,
          categories,
          fandoms,
          relationships,
          characters,
          freeformTags,
          link,
          started,
          updated,
          words,
          partsWritten = ficDoc.ChaptersWritten,
          // TODO: maybe add this info to airtable, maybe not
          chaptersPlanned = None,
          comments = None,
          kudos = None,
          hits = None,
          bookmarks = None
        )

    MyFicRecord(
      fic,
      airtable.id,
      MyFicStats(
        read = ficDoc.Read.getOrElse(false),
        backlog = ficDoc.Backlog.getOrElse(false),
        isOnKindle = ficDoc.IsOnKindle.getOrElse(false),
        readDates = ficDoc.ReadDates,
        kindleToDo = ficDoc.KindleToDo.getOrElse(false),
        quality = ficDoc.Quality.map(Quality.withName),
        comment = ficDoc.Comment
      )
    )
  }

  private def toRating(s: String) = s match
    case "E" => Rating.Explicit
    case "M" => Rating.Mature
    case "T" => Rating.Teen
    case "G" => Rating.General
    case _   => Rating.None

  def toMyStats(doc: StatsDocument): MyFicStats = MyFicStats(
    read = doc.Read.getOrElse(false),
    backlog = doc.Backlog.getOrElse(false),
    isOnKindle = doc.IsOnKindle.getOrElse(false),
    readDates = doc.ReadDates,
    kindleToDo = doc.KindleToDo.getOrElse(false),
    quality = doc.Quality.map(Quality.withName),
    comment = doc.Comment
  )

  def toStatsDoc(myFicStats: MyFicStats): StatsDocument = StatsDocument(
    Read = Some(myFicStats.read),
    Backlog = Some(myFicStats.backlog),
    IsOnKindle = Some(myFicStats.isOnKindle),
    ReadDates = myFicStats.readDates,
    KindleToDo = Some(myFicStats.kindleToDo),
    Quality = myFicStats.quality.map(q => q.toString),
    Comment = myFicStats.comment
  )
