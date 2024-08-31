package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.Ao3Url
import com.kapibarabanka.ao3scrapper.models.{
  ArchiveWarning,
  Category,
  Character,
  Fandom,
  FreeformTag,
  Rating,
  Relationship,
  RelationshipType
}
import com.kapibarabanka.kapibarabot.domain.{FicComment, MyFicModel, MyFicStats, Quality}

import java.time.LocalDate

object TestData:
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

  // Fics
  val angstyZoSan = MyFicModel(
    id = "1",
    isSeries = true,
    title = "Angsty Zosan",
    authors = List("author1"),
    rating = Rating.Explicit,
    warnings = Set(ArchiveWarning("Warning")),
    categories = Set(Category.MM),
    fandoms = Set(onePiece),
    characters = Set(zoro, sanji, luffy),
    relationships = List(romanticZoSan),
    tags = List(angst, slowBurn),
    link = Ao3Url.series("1"),
    started = LocalDate.of(2022, 11, 12),
    updated = Some(LocalDate.of(2023, 2, 4)),
    words = 100000,
    complete = false,
    partsWritten = 3
  )

  val friendly = MyFicModel(
    id = "2",
    isSeries = false,
    title = "Friendly",
    authors = List(),
    rating = Rating.Teen,
    warnings = Set(),
    categories = Set(Category.Gen),
    fandoms = Set(onePiece),
    characters = Set(zoro, sanji, luffy),
    relationships = List(mugivaras),
    tags = List(fluff, modernAu),
    link = Ao3Url.work("2"),
    started = LocalDate.of(2023, 11, 12),
    updated = None,
    words = 4000,
    complete = true,
    partsWritten = 1
  )
  val ratiorine = MyFicModel(
    id = "3",
    isSeries = false,
    title = "Angsty Zosan",
    authors = List("author3"),
    rating = Rating.Explicit,
    warnings = Set(),
    categories = Set(Category.MM),
    fandoms = Set(honkai),
    characters = Set(drRatio, aventurine),
    relationships = List(romaricRatiorine),
    tags = List(fluff, slowBurn),
    link = Ao3Url.work("3"),
    started = LocalDate.of(2024, 1, 3),
    updated = Some(LocalDate.of(2024, 2, 4)),
    words = 35000,
    complete = false,
    partsWritten = 5
  )

  val comment = FicComment(commentDate = "2024-08-30", comment = "Some comment")
  val readStats = MyFicStats(
    read = true,
    backlog = false,
    isOnKindle = true,
    readDates = Some("2024-08-31"),
    kindleToDo = false,
    quality = Some(Quality.Nice),
    fire = true,
    comment = None
  )
