package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.sqlite.{Sqlite, WithDb}
import com.kapibarabanka.kapibarabot.sqlite.docs.{FicsToTagsDoc, TagDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{FicsToTagsTable, TagsTable}
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
