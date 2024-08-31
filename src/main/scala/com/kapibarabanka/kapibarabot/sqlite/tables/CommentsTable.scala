package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.CommentDoc
import slick.jdbc.PostgresProfile.api.*

class CommentsTable(tag: Tag) extends Table[CommentDoc](tag, CommentsTable.name):
  def id          = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId       = column[String]("ficId")
  def commentDate = column[String]("commentDate")
  def comment     = column[String]("comment")

  def * = (id.?, ficId, commentDate, comment).mapTo[CommentDoc]

object CommentsTable extends MyTable:
  override val name: String     = "Comments"
  override val keyField: String = "id"

  def createIfNotExists = sqlu"""
      CREATE TABLE IF NOT EXISTS "#$name" (
      "id"	INTEGER NOT NULL UNIQUE,
      "ficId"	TEXT NOT NULL,
      "commentDate"	TEXT NOT NULL,
      "comment"	TEXT NOT NULL,
      PRIMARY KEY("id"),
      FOREIGN KEY("ficId") REFERENCES "#${FicsTable.name}"("#${FicsTable.keyField}") ON UPDATE CASCADE ON DELETE CASCADE);
      """
