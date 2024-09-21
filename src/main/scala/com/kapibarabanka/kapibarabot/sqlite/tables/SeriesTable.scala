package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.SeriesDoc
import slick.jdbc.PostgresProfile.api.*

class SeriesTable(tag: Tag) extends Table[SeriesDoc](tag, SeriesTable.name):
  def id       = column[String]("id", O.PrimaryKey, O.Unique)
  def title    = column[String]("title")
  def authors  = column[String]("authors")
  def link     = column[String]("link")
  def started  = column[String]("started")
  def updated  = column[Option[String]]("updated")
  def words    = column[Int]("words")
  def complete = column[Boolean]("complete")

  def backlog    = column[Boolean]("backlog")
  def isOnKindle = column[Boolean]("isOnKindle")

  def docCreated = column[String]("docCreated")

  def * = (
    id,
    title,
    authors,
    link,
    started,
    updated,
    words,
    complete,

    // stats
    backlog,
    isOnKindle,
    docCreated
  ).mapTo[SeriesDoc]

object SeriesTable extends MyTable:
  override val name: String     = "Series"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[SeriesTable].schema.createIfNotExists
