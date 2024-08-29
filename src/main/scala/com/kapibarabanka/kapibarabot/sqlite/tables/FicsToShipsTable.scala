package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.Sqlite
import com.kapibarabanka.kapibarabot.sqlite.docs.FicsToShipsDoc
import slick.jdbc.PostgresProfile.api.*

class FicsToShipsTable(tag: Tag) extends Table[FicsToShipsDoc](tag, FicsToShipsTable.name):
  def id       = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId    = column[String]("ficId")
  def shipName = column[String]("shipName")

  def * = (id.?, ficId, shipName).mapTo[FicsToShipsDoc]

object FicsToShipsTable extends MyTable:
  override val name: String     = "FicsToShips"
  override val keyField: String = "id"

  def createIfNotExists = Sqlite.createManyToManyTable(
    name = name,
    leftFieldName = "ficId",
    leftTable = FicsTable,
    rightFieldName = "shipName",
    rightTable = RelationshipsTable
  )
