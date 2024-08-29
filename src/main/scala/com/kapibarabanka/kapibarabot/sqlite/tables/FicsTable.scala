package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.FicDoc
import slick.jdbc.PostgresProfile.api.*

class FicsTable(tag: Tag) extends Table[FicDoc](tag, FicsTable.name):
  def id    = column[String]("id", O.PrimaryKey, O.Unique)
  def title = column[String]("title")

  def * = (id, title).mapTo[FicDoc]

object FicsTable extends MyTable:
  override val name: String     = "Fics"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[FicsTable].schema.createIfNotExists
