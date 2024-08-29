package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.Sqlite
import com.kapibarabanka.kapibarabot.sqlite.docs.FicsToCharactersDoc
import slick.jdbc.PostgresProfile.api.*

class FicsToCharactersTable(tag: Tag) extends Table[FicsToCharactersDoc](tag, FicsToCharactersTable.name):
  def id        = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId     = column[String]("ficId")
  def character = column[String]("character")

  def * = (id.?, ficId, character).mapTo[FicsToCharactersDoc]

object FicsToCharactersTable extends MyTable:
  override val name: String     = "FicsToCharacters"
  override val keyField: String = "id"
  
  def createIfNotExists = Sqlite.createManyToManyTable(
    name = name,
    leftFieldName = "ficId",
    leftTable = FicsTable,
    rightFieldName = "character",
    rightTable = CharactersTable
  )
