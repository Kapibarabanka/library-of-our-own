package com.kapibarabanka.kapibarabot.sqlite.docs

import slick.jdbc.PostgresProfile.api.*

case class TagDoc(name: String, category: Option[String])

class TagsTable(tag: Tag) extends Table[TagDoc](tag, "Tags"):
  def name     = column[String]("name", O.PrimaryKey, O.Unique)
  def category = column[Option[String]]("category")

  def * = (name, category).mapTo[TagDoc]

case class FicsToTagsDoc(id: Option[Int], ficId: String, tagName: String)

class FicsToTagsTable(tag: Tag) extends Table[FicsToTagsDoc](tag, "FicsToTags"):
  def id      = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId   = column[String]("ficId")
  def tagName = column[String]("tagName")

  def * = (id.?, ficId, tagName).mapTo[FicsToTagsDoc]
