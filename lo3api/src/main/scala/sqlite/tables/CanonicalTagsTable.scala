package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.CanonicalTagDoc

import slick.jdbc.PostgresProfile.api.*

class CanonicalTagsTable(tag: Tag) extends Table[CanonicalTagDoc](tag, TagsTable.name):
  def nameInWork    = column[String]("nameInWork", O.PrimaryKey, O.Unique)
  def canonicalName = column[String]("canonicalName")
  def filterable    = column[Boolean]("filterable")

  def * = (nameInWork, canonicalName, filterable).mapTo[CanonicalTagDoc]

object CanonicalTagsTable extends MyTable:
  override val name: String     = "CanonicalTags"
  override val keyField: String = "nameInWork"

  def createIfNotExists = TableQuery[CanonicalTagsTable].schema.createIfNotExists
