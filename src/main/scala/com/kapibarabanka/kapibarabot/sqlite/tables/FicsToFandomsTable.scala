package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.Sqlite
import com.kapibarabanka.kapibarabot.sqlite.docs.FicsToFandomsDoc
import slick.jdbc.PostgresProfile.api.*

class FicsToFandomsTable(tag: Tag) extends Table[FicsToFandomsDoc](tag, FicsToFandomsTable.name):
  def id     = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId  = column[String]("ficId")
  def fandom = column[String]("fandom")

  def * = (id.?, ficId, fandom).mapTo[FicsToFandomsDoc]

object FicsToFandomsTable extends MyTable:
  override val name: String     = "FicsToFandoms"
  override val keyField: String = "id"

  def createIfNotExists = Sqlite.createManyToManyTable(
    name = name,
    leftFieldName = "ficId",
    leftTable = FicsTable,
    rightFieldName = "fandom",
    rightTable = FandomsTable
  )
