package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.Sqlite
import com.kapibarabanka.kapibarabot.sqlite.docs.FicsToTagsDoc
import slick.jdbc.PostgresProfile.api.*

class FicsToTagsTable(tag: Tag) extends Table[FicsToTagsDoc](tag, FicsToTagsTable.name):
  def id      = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId   = column[String]("ficId")
  def tagName = column[String]("tagName")

  def * = (id.?, ficId, tagName).mapTo[FicsToTagsDoc]

object FicsToTagsTable extends MyTable:
  override val name: String     = "FicsToTags"
  override val keyField: String = "id"

  def createIfNotExists = Sqlite.createManyToManyTable(
    name = name,
    leftFieldName = "ficId",
    leftTable = FicsTable,
    rightFieldName = "tagName",
    rightTable = TagsTable
  )
