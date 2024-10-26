package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.ShipsToCharactersDoc
import sqlite.services.Lo3Db

import slick.jdbc.PostgresProfile.api.*

class ShipsToCharactersTable(tag: Tag) extends Table[ShipsToCharactersDoc](tag, ShipsToCharactersTable.name):
  def id        = column[Int]("id", O.PrimaryKey, O.Unique)
  def shipName  = column[String]("shipName")
  def character = column[String]("character")

  def * = (id.?, shipName, character).mapTo[ShipsToCharactersDoc]

object ShipsToCharactersTable extends MyTable:
  override val name: String     = "ShipsToCharacters"
  override val keyField: String = "id"

  def createIfNotExists = Lo3Db.createManyToManyTableAction(
    name = name,
    leftFieldName = "shipName",
    leftTable = RelationshipsTable,
    rightFieldName = "character",
    rightTable = CharactersTable
  )
