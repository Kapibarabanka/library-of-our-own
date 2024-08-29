package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.CharacterDoc
import slick.jdbc.PostgresProfile.api.*

class CharactersTable(tag: Tag) extends Table[CharacterDoc](tag, CharactersTable.name):
  def fullName = column[String]("fullName", O.PrimaryKey)
  def name     = column[String]("name")
  def label    = column[Option[String]]("label")

  def * = (fullName, name, label).mapTo[CharacterDoc]

object CharactersTable extends MyTable:
  override val name: String     = "Characters"
  override val keyField: String = "fullName"

  def createIfNotExists = TableQuery[CharactersTable].schema.createIfNotExists
