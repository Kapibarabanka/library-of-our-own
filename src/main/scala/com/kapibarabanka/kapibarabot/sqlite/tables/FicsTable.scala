package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.FicDoc
import slick.jdbc.PostgresProfile.api.*

class FicsTable(tag: Tag) extends Table[FicDoc](tag, FicsTable.name):
  def id       = column[String]("id", O.PrimaryKey, O.Unique)
  def isSeries = column[Boolean]("isSeries")
  def title    = column[String]("title")

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
  def kindleToDo = column[Boolean]("kindleToDo")
  def readDates  = column[Option[String]]("readDates")
  def quality    = column[Option[String]]("quality")
  def fire       = column[Boolean]("fire")

  def docCreated = column[String]("docCreated")

  def * = (
    id,
    isSeries,
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
    kindleToDo,
    readDates,
    quality,
    fire,
    docCreated
  ).mapTo[FicDoc]

object FicsTable extends MyTable:
  override val name: String     = "Fics"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[FicsTable].schema.createIfNotExists
