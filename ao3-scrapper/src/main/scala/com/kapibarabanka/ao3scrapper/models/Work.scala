package com.kapibarabanka.ao3scrapper.models

import java.util.Date

case class Work(
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
  started: Date,
  updated: Option[Date],
  words: Long,
  partsWritten: Int,
  chaptersPlanned: Option[Int],
  comments: Option[Long],
  kudos: Option[Long],
  hits: Option[Long],
  bookmarks: Option[Long],
) extends Fic:
  override val ficType: FicType = FicType.Work

  val complete: Boolean = chaptersPlanned match
    case Some(planned) => partsWritten >= planned
    case None => false
