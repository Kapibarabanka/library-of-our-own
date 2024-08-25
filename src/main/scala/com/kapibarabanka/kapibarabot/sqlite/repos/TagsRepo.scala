package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.sqlite.docs.{FicsToTagsDoc, FicsToTagsTable, TagDoc, TagsTable}
import slick.jdbc.PostgresProfile.api.*
import zio.ZIO

class TagsRepo(userId: String) extends WithDb(userId):
  private val allTags    = TableQuery[TagsTable]
  private val ficsToTags = TableQuery[FicsToTagsTable]

  private def addQuery(tag: String) =
    sqlu"INSERT OR IGNORE INTO Tags (name, category) VALUES ($tag, NULL)"

  def add(tag: String) = db(addQuery(tag))

  def add(tags: List[String]) = db(DBIO.sequence(tags.map(t => addQuery(t))))

  def addAndLink(tags: List[String], ficId: String) = for {
    _ <- add(tags)
    _ <- db(ficsToTags ++= tags.map(tag => FicsToTagsDoc(None, ficId, tag)))
  } yield ()

  def getAll: ZIO[Any, Throwable, List[TagDoc]] = db(allTags.result).map(_.toList)

  def getFicTags(ficId: String) = db(ficsToTags.filter(r => r.ficId === ficId).map(_.tagName).result)

  def initIfNotExists = db(
    DBIO.seq(
      allTags.schema.createIfNotExists,
      sqlu"""
    CREATE TABLE IF NOT EXISTS "FicsToTags" (
	"id"	INTEGER NOT NULL UNIQUE,
	"ficId"	TEXT NOT NULL,
	"tagName"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("ficId") REFERENCES "Fics"("id") ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY("tagName") REFERENCES "Tags"("name") ON UPDATE CASCADE ON DELETE CASCADE);
    """
    )
  )
