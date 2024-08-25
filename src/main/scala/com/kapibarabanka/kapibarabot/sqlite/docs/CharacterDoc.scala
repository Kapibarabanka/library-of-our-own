package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.StringUtils
import slick.jdbc.PostgresProfile.api.*
import com.kapibarabanka.ao3scrapper.models.Character

case class CharacterDoc(fullName: String, name: String, label: Option[String])

object CharacterDoc:
  def fromModel(model: Character): CharacterDoc =
    CharacterDoc(StringUtils.combineWithLabel(model.name, model.label), model.name, model.label)

class CharactersTable(tag: Tag) extends Table[CharacterDoc](tag, "Characters"):
  def fullName = column[String]("fullName", O.PrimaryKey)
  def name     = column[String]("name")
  def label    = column[Option[String]]("label")

  def * = (fullName, name, label).mapTo[CharacterDoc]

case class FicsToCharactersDoc(id: Option[Int], ficId: String, character: String)

class FicsToCharactersTable(tag: Tag) extends Table[FicsToCharactersDoc](tag, "FicsToCharacters"):
  def id        = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId     = column[String]("ficId")
  def character = column[String]("character")

  def * = (id.?, ficId, character).mapTo[FicsToCharactersDoc]
