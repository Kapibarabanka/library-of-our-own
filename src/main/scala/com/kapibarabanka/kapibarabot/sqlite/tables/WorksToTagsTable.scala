package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.sqlite.docs.WorksToTagsDoc
import slick.jdbc.PostgresProfile.api.*

class WorksToTagsTable(tag: Tag) extends Table[WorksToTagsDoc](tag, WorksToTagsTable.name):
  def id      = column[Int]("id", O.PrimaryKey, O.Unique)
  def workId   = column[String]("workId")
  def tagName = column[String]("tagName")

  def * = (id.?, workId, tagName).mapTo[WorksToTagsDoc]

object WorksToTagsTable extends MyTable:
  override val name: String     = "WorksToTags"
  override val keyField: String = "id"

  def createIfNotExists = KapibarabotDb.createManyToManyTableAction(
    name = name,
    leftFieldName = "workId",
    leftTable = WorksTable,
    rightFieldName = "tagName",
    rightTable = TagsTable
  )
