package kapibarabanka.lo3.bot
package sqlite.tables

import sqlite.docs.CommentDoc
import slick.jdbc.PostgresProfile.api.*

class CommentsTable(tag: Tag) extends Table[CommentDoc](tag, CommentsTable.name):
  def id          = column[Int]("id", O.PrimaryKey, O.Unique)
  def userId      = column[String]("userId")
  def ficId       = column[String]("ficId")
  def ficIsSeries = column[Boolean]("ficIsSeries")
  def commentDate = column[String]("commentDate")
  def comment     = column[String]("comment")

  def * = (id.?, userId, ficId, ficIsSeries, commentDate, comment).mapTo[CommentDoc]

object CommentsTable extends MyTable:
  override val name: String     = "Comments"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[CommentsTable].schema.createIfNotExists
