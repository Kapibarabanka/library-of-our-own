package kapibarabanka.lo3.bot
package sqlite.tables

import sqlite.docs.FandomDoc
import slick.jdbc.PostgresProfile.api.*

class FandomsTable(tag: Tag) extends Table[FandomDoc](tag, FandomsTable.name):
  def fullName = column[String]("fullName", O.PrimaryKey)
  def name     = column[String]("name")
  def label    = column[Option[String]]("label")

  def * = (fullName, name, label).mapTo[FandomDoc]

object FandomsTable extends MyTable:
  override val name: String     = "Fandoms"
  override val keyField: String = "fullName"

  def createIfNotExists = TableQuery[FandomsTable].schema.createIfNotExists
