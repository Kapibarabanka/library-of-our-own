package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.Ao3Url
import com.kapibarabanka.ao3scrapper.models.{
  ArchiveWarning,
  Category,
  Character,
  Fandom,
  FreeformTag,
  Published,
  PublishedAndUpdated,
  Rating,
  Relationship,
  RelationshipType,
  Series,
  SingleDate,
  Work
}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDetails, Quality}

import java.time.LocalDate

object TestData:
  val userId1 = "111"
  val userId2 = "222"
  
  // Fandoms
  val onePiece = Fandom("One Piece", Some("Anime and Manga"))
  val honkai   = Fandom("Honkai: Star Rail", None)

  // Characters
  val zoro       = Character("Zoro", None)
  val sanji      = Character("Sanji", Some("One Piece"))
  val luffy      = Character("Luffy", None)
  val drRatio    = Character("Dr. Ratio", Some("Honkai"))
  val aventurine = Character("Aventurine", None)

  // Ships
  val romanticZoSan    = Relationship(Set(zoro, sanji), RelationshipType.Romantic)
  val mugivaras        = Relationship(Set(luffy, zoro, sanji), RelationshipType.Platonic)
  val romaricRatiorine = Relationship(Set(drRatio, aventurine), RelationshipType.Romantic)

  // Tags
  val fluff    = FreeformTag("Fluff", Some(true))
  val angst    = FreeformTag("Angst", Some(true))
  val modernAu = FreeformTag("Modern AU", Some(true))
  val slowBurn = FreeformTag("Slow Burn", Some(true))

  // Works
  val angstyZoSan = Work(
    id = "1",
    title = "Angsty Zosan",
    authors = List("author1", "author2"),
    rating = Rating.Explicit,
    warnings = Set(ArchiveWarning("Warning")),
    categories = Set(Category.MM),
    fandoms = Set(onePiece),
    characters = Set(zoro, sanji, luffy),
    relationships = List(romanticZoSan),
    freeformTags = List(angst, slowBurn),
    link = Ao3Url.work("1"),
    date = PublishedAndUpdated(LocalDate.of(2022, 11, 12), LocalDate.of(2023, 2, 4)),
    words = 100000,
    chaptersWritten = 3,
    chaptersPlanned = None,
    comments = None,
    kudos = None,
    hits = None,
    bookmarks = None
  )

  val friendly = Work(
    id = "2",
    title = "Friendly",
    authors = List("author1"),
    rating = Rating.Teen,
    warnings = Set(),
    categories = Set(Category.Gen),
    fandoms = Set(onePiece),
    characters = Set(zoro, sanji, luffy),
    relationships = List(mugivaras),
    freeformTags = List(fluff, modernAu),
    link = Ao3Url.work("2"),
    date = Published(LocalDate.of(2023, 11, 12)),
    words = 4000,
    chaptersWritten = 1,
    chaptersPlanned = Some(1),
    comments = None,
    kudos = None,
    hits = None,
    bookmarks = None
  )
  val ratiorine = Work(
    id = "3",
    title = "Ratiorine",
    authors = List(),
    rating = Rating.Explicit,
    warnings = Set(),
    categories = Set(Category.MM),
    fandoms = Set(honkai),
    characters = Set(drRatio, aventurine),
    relationships = List(romaricRatiorine),
    freeformTags = List(fluff, slowBurn),
    link = Ao3Url.work("3"),
    date = PublishedAndUpdated(LocalDate.of(2024, 1, 3), LocalDate.of(2024, 2, 4)),
    words = 35000,
    chaptersWritten = 5,
    chaptersPlanned = Some(10),
    comments = None,
    kudos = None,
    hits = None,
    bookmarks = None
  )

  val opSeries = Series(
    id = "1",
    link = Ao3Url.series("1"),
    title = "OP series",
    authors = List("author1", "author2"),
    started = LocalDate.of(2022, 11, 12),
    updated = Some(LocalDate.of(2023, 11, 12)),
    words = 104000,
    complete = false,
    bookmarks = None,
    description = None,
    works = List(friendly, angstyZoSan)
  )

  val comment = FicComment(commentDate = "2024-08-30", comment = "Some comment")
