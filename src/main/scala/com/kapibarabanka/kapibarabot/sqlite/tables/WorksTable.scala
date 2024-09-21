package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.WorkDoc
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

  def read       = column[Boolean]("read")
  def backlog    = column[Boolean]("backlog")
  def isOnKindle = column[Boolean]("isOnKindle")
  def readDates  = column[Option[String]]("readDates")
  def quality    = column[Option[String]]("quality")
  def fire       = column[Boolean]("fire")

  def docCreated = column[String]("docCreated")

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
    partsWritten,

    // stats
    read,
    backlog,
    isOnKindle,
    readDates,
    quality,
    fire,
    docCreated
  ).mapTo[WorkDoc]

object WorksTable extends MyTable:
  override val name: String     = "Works"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[WorksTable].schema.createIfNotExists
