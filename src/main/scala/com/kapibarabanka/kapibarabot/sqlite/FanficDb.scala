package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.kapibarabot.sqlite.tables.*
import slick.jdbc.PostgresProfile.api.*

class FanficDb(userId: String) extends WithDb(userId):
  val fics = FicsRepo(userId)

  val allTables: List[MyTable] = List(
    FicsTable,
    FandomsTable,
    FicsToFandomsTable,
    CharactersTable,
    FicsToCharactersTable,
    RelationshipsTable,
    ShipsToCharactersTable,
    FicsToShipsTable,
    TagsTable,
    FicsToTagsTable,
    CommentsTable
  )

  def init = for {
    _ <- db(DBIO.sequence(allTables.map(_.createIfNotExists)))
  } yield ()

  def beginWithTestData = {
    for {
      _ <- db(DBIO.sequence(allTables.map(_.dropIfExists)))
      _ <- init
      _ <- fics.addFic(TestData.angstyZoSan)
      _ <- fics.addComment(TestData.angstyZoSan.id, TestData.comment)
      _ <- fics.addFic(TestData.friendly)
      _ <- fics.addFic(TestData.ratiorine)
      _ <- fics.patchStats(TestData.ratiorine.id, TestData.readStats)
    } yield ()
  }
