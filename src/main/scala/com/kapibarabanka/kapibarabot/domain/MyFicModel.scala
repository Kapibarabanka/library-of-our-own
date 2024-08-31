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
