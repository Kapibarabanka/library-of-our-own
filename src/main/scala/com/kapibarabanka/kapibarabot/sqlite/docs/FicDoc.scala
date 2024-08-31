package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.{ArchiveWarning, Category, Character, Fandom, FreeformTag, Rating, Relationship}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel, MyFicModel, MyFicStats, Quality}

import java.time.LocalDate

case class FicDoc(
    id: String,
    isSeries: Boolean,
    title: String,
    authors: String,
    rating: String,
    warnings: String,
    categories: String,
    link: String,
    started: String,
    updated: Option[String],
    words: Int,
    complete: Boolean,
    partsWritten: Int,

    // stats
    read: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    readDates: Option[String],
    quality: Option[String],
    fire: Boolean,
    
    docCreated: String
):
  def toModel(
      fandoms: Iterable[Fandom],
      characters: Iterable[Character],
      relationships: Iterable[Relationship],
      tags: Iterable[FreeformTag]
  ): MyFicModel =
    MyFicModel(
      id = id,
      isSeries = isSeries,
      title = title,
      authors = authors.split(", ").toList,
      rating = Rating.withName(rating),
      warnings = warnings.split(", ").toSet.map(ArchiveWarning(_)),
      categories = categories.split(", ").map(Category.withName).toSet,
      fandoms = fandoms.toSet,
      tags = tags.toList,
      characters = characters.toSet,
      relationships = relationships.toList,
      link = link,
      started = LocalDate.parse(started),
      updated = updated.map(LocalDate.parse),
      words = words,
      complete = complete,
      partsWritten = partsWritten
    )

  def toDisplayModel(
      fandoms: Iterable[String],
      characters: Iterable[String],
      relationships: Iterable[String],
      tags: Iterable[String],
      comments: Iterable[FicComment]
  ): FicDisplayModel =
    FicDisplayModel(
      id = id,
      title = title,
      authors = authors.split(", ").toList,
      fandoms = fandoms.toSet,
      characters = characters.toSet,
      relationships = relationships.toList,
      tags = tags.toList,
      comments = comments.toList,
      words = words,
      stats = MyFicStats(
        read = read,
        backlog = backlog,
        isOnKindle = isOnKindle,
        readDates = readDates,
        kindleToDo = false,
        quality = quality.map(Quality.withName),
        fire = fire,
        comment = None
      )
    )

object FicDoc:
  def fromModel(fic: MyFicModel): FicDoc = FicDoc(
    id = fic.id,
    isSeries = fic.isSeries,
    title = fic.title,
    authors = {
      fic.authors match
        case Nil    => "Anonymous"
        case values => values.mkString(", ")
    },
    rating = fic.rating.toString,
    warnings = fic.warnings.map(_.name).mkString(", "),
    categories = fic.categories.map(_.toString).mkString(", "),
    started = fic.started.toString,
    updated = fic.updated.map(_.toString),
    words = fic.words,
    link = fic.link,
    complete = fic.complete,
    partsWritten = fic.partsWritten,
    read = false,
    backlog = false,
    isOnKindle = false,
    readDates = None,
    quality = None,
    fire = false,
    docCreated = LocalDate.now().toString
  )
