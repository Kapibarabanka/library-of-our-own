package com.kapibarabanka.ao3scrapper.models

import java.time.LocalDate

case class Series(
    id: String,
    title: String,
    authors: List[String],
    rating: Rating.Value,
    warnings: Set[ArchiveWarning],
    categories: Set[Category.Value],
    fandoms: Set[Fandom],
    relationships: List[Relationship],
    characters: Set[Character],
    freeformTags: List[FreeformTag],
    link: String,
    started: LocalDate,
    updated: Option[LocalDate],
    words: Int,
    complete: Boolean,
    bookmarks: Option[Int],
    workIds: List[String],
    description: Option[String]
) extends Fic:
  override val ficType: FicType  = FicType.Series
  override val partsWritten: Int = workIds.size

//  val warnings: Set[ArchiveWarning] = works.flatMap(w => w.warnings).toSet
//  val rating: Rating = works.map(w => w.rating).maxBy(r => r.ordinal)
//  val categories: Set[Category] = works.flatMap(w => w.categories).toSet
//  val fandoms: Set[Fandom] = works.flatMap(w => w.fandoms).toSet
//  val relationships: List[Relationship] = works.flatMap(w => w.relationships).distinct
//  val characters: Set[Character] = works.flatMap(w => w.characters).toSet
//  val freeformTags: List[FreeformTag] = works.flatMap(w => w.freeformTags).distinct
