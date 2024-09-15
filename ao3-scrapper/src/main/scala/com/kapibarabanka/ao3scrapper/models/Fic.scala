package com.kapibarabanka.ao3scrapper.models

import java.time.LocalDate

trait Fic:
  val id: String
  val title: String
  val authors: List[String]
  val rating: Rating.Value
  val warnings: Set[ArchiveWarning]
  val categories: Set[Category.Value]
  val fandoms: Set[Fandom]
  val relationships: List[Relationship]
  val characters: Set[Character]
  val freeformTags: List[FreeformTag]
  val link: String
  val started: LocalDate
  val updated: Option[LocalDate]
  val words: Int
  val complete: Boolean
  val bookmarks: Option[Int]
  val ficType: FicType
  val partsWritten: Int
