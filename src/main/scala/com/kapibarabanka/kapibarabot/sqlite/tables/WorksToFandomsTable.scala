package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.sqlite.docs.WorksToFandomsDoc
import slick.jdbc.PostgresProfile.api.*

class WorksToFandomsTable(tag: Tag) extends Table[WorksToFandomsDoc](tag, WorksToFandomsTable.name):
  def id     = column[Int]("id", O.PrimaryKey, O.Unique)
  def workId  = column[String]("workId")
  def fandom = column[String]("fandom")

  def * = (id.?, workId, fandom).mapTo[WorksToFandomsDoc]

object WorksToFandomsTable extends MyTable:
  override val name: String     = "WorksToFandoms"
  override val keyField: String = "id"

  def createIfNotExists = KapibarabotDb.createManyToManyTableAction(
    name = name,
    leftFieldName = "workId",
    leftTable = WorksTable,
    rightFieldName = "fandom",
    rightTable = FandomsTable
  )
