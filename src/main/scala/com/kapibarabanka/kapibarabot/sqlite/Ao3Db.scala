package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models.Character
import com.kapibarabanka.kapibarabot.domain.MyFicModel
import com.kapibarabanka.kapibarabot.sqlite.repos.*
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import slick.jdbc.PostgresProfile.api.*

class Ao3Db(userId: String):
  val tags       = TagsRepo(userId)
  val fics       = FicsRepo(userId)
  val characters = CharactersRepo(userId)

  private def db[T] = Sqlite.run[T](userId)

  def init = for {
    _ <- tags.initIfNotExists
    _ <- fics.initIfNotExists
    _ <- characters.initIfNotExists
  } yield ()

  def beginWithTestData = {
    for {
      _ <- db(sqlu"DROP TABLE IF EXISTS Tags")
      _ <- db(sqlu"DROP TABLE IF EXISTS Fics")
      _ <- db(sqlu"DROP TABLE IF EXISTS FicsToTags")
      _ <- db(sqlu"DROP TABLE IF EXISTS Characters")
      _ <- init
      _ <- fics.add(
        MyFicModel("1", "fluffy au", List("Fluff", "AU"), List(Character("Zoro", None), Character("Sanji", Some("One Piece"))))
      )
      _ <- fics.add(MyFicModel("2", "angsty au", List("Angst", "AU"), List(Character("Sanji", Some("One Piece")))))
    } yield ()
  }
