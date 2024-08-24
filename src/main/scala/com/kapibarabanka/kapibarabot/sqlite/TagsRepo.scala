package com.kapibarabanka.kapibarabot.sqlite

import slick.jdbc.PostgresProfile.api.*

case class FreeformTag(name: String, category: String = "")

class TagsTable(tag: Tag) extends Table[(String, String)](tag, "Tags") {
  def name     = column[String]("name", O.PrimaryKey, O.Unique)
  def category = column[String]("category")

  def * = (name, category)
}

class TagsRepo(userId: String) extends WithDb(userId):
  private val tags  = TableQuery[TagsTable]
  
  private def addQuery(tag: String) =
    sqlu"INSERT OR IGNORE INTO Tags (name, category) VALUES ($tag, '')"

  def add(tag: String) = db(addQuery(tag))

  def add(tags: List[String]) = db(DBIO.sequence(tags.map(t => addQuery(t))))

  def delete(tag: String) = db(tags.filter(_.name === tag).delete)

  def getAll = for {
    sqlResults <- db(tags.result)
  } yield sqlResults.map { case (name, category) => FreeformTag(name, category) }.toList

  def initIfNotExists = db(DBIO.seq(tags.schema.createIfNotExists))
