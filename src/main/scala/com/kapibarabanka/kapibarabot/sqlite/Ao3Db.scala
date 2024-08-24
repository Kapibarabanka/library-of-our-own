package com.kapibarabanka.kapibarabot.sqlite

import slick.jdbc.PostgresProfile.api.*

class Ao3Db(userId: String):
  val tags = TagsRepo(userId)
  val fics = FicsRepo(userId)

  private def db[T] = Sqlite.run[T](userId)

  def init = for {
    _ <- tags.initIfNotExists
    _ <- fics.initIfNotExists
  } yield ()

  def beginWithTestData = {
    val tagsTable       = TableQuery[TagsTable]
    val ficsTable       = TableQuery[FicsTable]
    val ficsToTagsTable = TableQuery[FicsToTagsTable]
    for {
      _ <- db(sqlu"DROP TABLE Tags")
      _ <- db(sqlu"DROP TABLE Fics")
      _ <- db(sqlu"DROP TABLE FicsToTags")
      _ <- init
      _ <- fics.add(MyFicModel("1", "fluffy au", List("Fluff", "AU")))
      _ <- fics.add(MyFicModel("2", "angsty au", List("Angst", "AU")))
    } yield ()
  }
