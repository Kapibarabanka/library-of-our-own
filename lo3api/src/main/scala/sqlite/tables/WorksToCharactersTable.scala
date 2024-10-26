package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.WorksToCharactersDoc
import sqlite.services.Lo3Db

import slick.jdbc.PostgresProfile.api.*

class WorksToCharactersTable(tag: Tag) extends Table[WorksToCharactersDoc](tag, WorksToCharactersTable.name):
  def id        = column[Int]("id", O.PrimaryKey, O.Unique)
  def workId    = column[String]("workId")
  def character = column[String]("character")

  def * = (id.?, workId, character).mapTo[WorksToCharactersDoc]

object WorksToCharactersTable extends MyTable:
  override val name: String     = "WorksToCharacters"
  override val keyField: String = "id"

  def createIfNotExists = Lo3Db.createManyToManyTableAction(
    name = name,
    leftFieldName = "workId",
    leftTable = WorksTable,
    rightFieldName = "character",
    rightTable = CharactersTable
  )
