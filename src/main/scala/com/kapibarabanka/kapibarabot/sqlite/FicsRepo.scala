package com.kapibarabanka.kapibarabot.sqlite

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ProvenShape, Tag}
import zio.{Task, ZIO}

case class MyFicModel(id: String, title: String, tags: List[String])

case class FicDoc(id: String, title: String)

class FicsTable(tag: Tag) extends Table[FicDoc](tag, "Fics"):
  def id    = column[String]("id", O.PrimaryKey, O.Unique)
  def title = column[String]("title")

  def * = (id, title).mapTo[FicDoc]

class FicsToTagsTable(tag: Tag) extends Table[(Option[Int], String, String)](tag, "FicsToTags"):
  def id      = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId   = column[String]("ficId")
  def tagName = column[String]("tagName")

  def * = (id.?, ficId, tagName)

class FicsRepo(userId: String) extends WithDb(userId):
  private val fics       = TableQuery[FicsTable]
  private val ficsToTags = TableQuery[FicsToTagsTable]
  private val tagsRepo   = TagsRepo(userId)

  private def toDoc(fic: MyFicModel)                     = FicDoc(id = fic.id, title = fic.title)
  private def toModel(ficDoc: FicDoc, tags: Seq[String]) = MyFicModel(id = ficDoc.id, title = ficDoc.title, tags = tags.toList)

  def add(fic: MyFicModel): ZIO[Any, Throwable, Unit] = for {
    _ <- tagsRepo.add(fic.tags)
    _ <- db(fics += toDoc(fic))
    _ <- db(ficsToTags ++= fic.tags.map(tag => (None, fic.id, tag)))
  } yield ()

  def getById(ficId: String): ZIO[Any, Throwable, MyFicModel] = for {
    tags   <- db(ficsToTags.filter(r => r.ficId === ficId).map(_.tagName).result)
    ficDoc <- db(fics.filter(f => f.id === ficId).result).map(_.head)
  } yield toModel(ficDoc, tags)

  override def initIfNotExists: Task[Unit] = db(
    DBIO.seq(
      fics.schema.createIfNotExists,
      sqlu"""
    CREATE TABLE "FicsToTags" (
	"id"	INTEGER NOT NULL UNIQUE,
	"ficId"	TEXT NOT NULL,
	"tagName"	TEXT NOT NULL,
	PRIMARY KEY("id" AUTOINCREMENT),
	FOREIGN KEY("ficId") REFERENCES "Fics"("id") ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY("tagName") REFERENCES "Tags"("name") ON UPDATE CASCADE ON DELETE CASCADE);
    """
    )
  )
