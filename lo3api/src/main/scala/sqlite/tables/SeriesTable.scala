package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.SeriesDoc

import slick.jdbc.PostgresProfile.api.*

class SeriesTable(tag: Tag) extends Table[SeriesDoc](tag, SeriesTable.name):
  def id       = column[String]("id", O.PrimaryKey, O.Unique)
  def title    = column[String]("title")
  def authors  = column[String]("authors")
  def link     = column[String]("link")
  def started  = column[String]("started")
  def updated  = column[Option[String]]("updated")
  def complete = column[Boolean]("complete")

  def * = (
    id,
    title,
    authors,
    link,
    started,
    updated,
    complete
  ).mapTo[SeriesDoc]

object SeriesTable extends MyTable:
  override val name: String     = "Series"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[SeriesTable].schema.createIfNotExists
