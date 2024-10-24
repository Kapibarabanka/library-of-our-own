package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.TagDoc

import slick.jdbc.PostgresProfile.api.*

class TagsTable(tag: Tag) extends Table[TagDoc](tag, TagsTable.name):
  def name       = column[String]("name", O.PrimaryKey, O.Unique)
  def category   = column[Option[String]]("category")
  def filterable = column[Boolean]("filterable")

  def * = (name, category, filterable).mapTo[TagDoc]

object TagsTable extends MyTable:
  override val name: String     = "Tags"
  override val keyField: String = "name"

  def createIfNotExists = TableQuery[TagsTable].schema.createIfNotExists
