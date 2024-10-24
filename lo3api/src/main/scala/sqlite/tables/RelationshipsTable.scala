package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.RelationshipDoc

import slick.jdbc.PostgresProfile.api.*

class RelationshipsTable(tag: Tag) extends Table[RelationshipDoc](tag, RelationshipsTable.name):
  def name             = column[String]("name", O.PrimaryKey)
  def relationshipType = column[String]("relationshipType")
  def nameInFic        = column[Option[String]]("nameInFic")

  def * = (name, relationshipType, nameInFic).mapTo[RelationshipDoc]

object RelationshipsTable extends MyTable:
  override val name: String     = "Relationships"
  override val keyField: String = "name"

  def createIfNotExists = TableQuery[RelationshipsTable].schema.createIfNotExists
