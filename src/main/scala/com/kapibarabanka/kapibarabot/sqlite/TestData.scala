package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models.{Character, Fandom, Relationship, RelationshipType}
import com.kapibarabanka.kapibarabot.domain.MyFicModel

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
  val fluff    = "Fluff"
  val angst    = "Angst"
  val modernAu = "Modern AU"
  val slowBurn = "Slow Burn"

  // Fics
  val angstyZoSan = MyFicModel(
    id = "1",
    title = "Angsty ZoSan",
    fandoms = Set(onePiece),
    characters = Set(zoro, sanji, luffy),
    relationships = List(romanticZoSan),
    tags = List(angst, slowBurn)
  )

  val friendly = MyFicModel(
    id = "2",
    title = "Friendly",
    fandoms = Set(onePiece),
    characters = Set(zoro, sanji, luffy),
    relationships = List(mugivaras),
    tags = List(fluff, modernAu)
  )

  val ratiorine = MyFicModel(
    id = "3",
    title = "Ratiorine",
    fandoms = Set(honkai),
    characters = Set(drRatio, aventurine),
    relationships = List(romaricRatiorine),
    tags = List(fluff, slowBurn)
  )
