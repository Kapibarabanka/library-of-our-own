package kapibarabanka.lo3.bot
package sqlite.tables

import sqlite.docs.WorkDoc
import slick.jdbc.PostgresProfile.api.*

class WorksTable(tag: Tag) extends Table[WorkDoc](tag, WorksTable.name):
  def id    = column[String]("id", O.PrimaryKey, O.Unique)
  def title = column[String]("title")

  def authors      = column[String]("authors")
  def rating       = column[String]("rating")
  def warnings     = column[String]("warnings")
  def categories   = column[String]("categories")
  def link         = column[String]("link")
  def started      = column[String]("started")
  def updated      = column[Option[String]]("updated")
  def words        = column[Int]("words")
  def complete     = column[Boolean]("complete")
  def partsWritten = column[Int]("partsWritten")

  def * = (
    id,
    title,
    authors,
    rating,
    warnings,
    categories,
    link,
    started,
    updated,
    words,
    complete,
    partsWritten
  ).mapTo[WorkDoc]

object WorksTable extends MyTable:
  override val name: String     = "Works"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[WorksTable].schema.createIfNotExists
