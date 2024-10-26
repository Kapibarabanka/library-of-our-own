package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.WorksToShipsDoc
import sqlite.services.Lo3Db

import slick.jdbc.PostgresProfile.api.*

class WorksToShipsTable(tag: Tag) extends Table[WorksToShipsDoc](tag, WorksToShipsTable.name):
  def id       = column[Int]("id", O.PrimaryKey, O.Unique)
  def workId    = column[String]("workId")
  def shipName = column[String]("shipName")

  def * = (id.?, workId, shipName).mapTo[WorksToShipsDoc]

object WorksToShipsTable extends MyTable:
  override val name: String     = "WorksToShips"
  override val keyField: String = "id"

  def createIfNotExists = Lo3Db.createManyToManyTableAction(
    name = name,
    leftFieldName = "workId",
    leftTable = WorksTable,
    rightFieldName = "shipName",
    rightTable = RelationshipsTable
  )
