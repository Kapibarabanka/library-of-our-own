package com.kapibarabanka.ao3scrapper.domain

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
    date: WorkDate,
    words: Int,
    chaptersWritten: Int,
    chaptersPlanned: Option[Int],
    comments: Option[Int],
    kudos: Option[Int],
    hits: Option[Int],
    bookmarks: Option[Int]
):
  val complete: Boolean = chaptersPlanned match
    case Some(planned) => chaptersWritten >= planned
    case None          => false
