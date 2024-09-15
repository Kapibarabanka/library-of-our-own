package com.kapibarabanka.kapibarabot.domain

import com.kapibarabanka.ao3scrapper.models.*

import java.time.LocalDate

case class MyFicModel(
    id: String,
    isSeries: Boolean,
    title: String,
    authors: List[String],
    rating: Rating.Value,
    warnings: Set[ArchiveWarning],
    categories: Set[Category.Value],
    fandoms: Set[Fandom],
    characters: Set[Character],
    relationships: List[Relationship],
    tags: List[FreeformTag],
    link: String,
    started: LocalDate,
    updated: Option[LocalDate],
    words: Int,
    complete: Boolean,
    partsWritten: Int
)

object MyFicModel:
  def fromWork(work: Work) = MyFicModel(
    id = work.id,
    isSeries = false,
    title = work.title,
    authors = work.authors,
    rating = work.rating,
    warnings = work.warnings,
    categories = work.categories,
    fandoms = work.fandoms,
    characters = work.characters,
    relationships = work.relationships,
    tags = work.freeformTags,
    link = work.link,
    started = work.started,
    updated = work.updated,
    words = work.words,
    complete = work.complete,
    partsWritten = work.partsWritten
  )

  def fromSeries(series: Series) = MyFicModel(
    id = series.id,
    isSeries = true,
    title = series.title,
    authors = series.authors,
    rating = series.rating,
    warnings = series.warnings,
    categories = series.categories,
    fandoms = series.fandoms,
    characters = series.characters,
    relationships = series.relationships,
    tags = series.freeformTags,
    link = series.link,
    started = series.started,
    updated = series.updated,
    words = series.words,
    complete = series.complete,
    partsWritten = series.workIds.length
  )
