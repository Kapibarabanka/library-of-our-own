package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.Character
import com.kapibarabanka.kapibarabot.domain.MyFicModel
import com.kapibarabanka.kapibarabot.sqlite.*
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import slick.jdbc.PostgresProfile.api.*
import zio.{Task, ZIO}

class FicsRepo(userId: String) extends WithDb(userId):
  private val fics           = TableQuery[FicsTable]
  private val tagsRepo       = TagsRepo(userId)
  private val charactersRepo = CharactersRepo(userId)

  private def toModel(ficDoc: FicDoc, tags: Seq[String], characters: Seq[Character]) =
    MyFicModel(id = ficDoc.id, title = ficDoc.title, tags = tags.toList, characters = characters.toList)

  def add(fic: MyFicModel): ZIO[Any, Throwable, Unit] = for {
    _ <- db(fics += FicDoc.fromModel(fic))
    _ <- tagsRepo.addAndLink(fic.tags, fic.id)
    _ <- charactersRepo.addAndLink(fic.characters, fic.id)
  } yield ()

  def getById(ficId: String): ZIO[Any, Throwable, MyFicModel] = for {
    tags   <- tagsRepo.getFicTags(ficId)
    ficDoc <- db(fics.filter(f => f.id === ficId).result).map(_.head)
  } yield toModel(ficDoc, tags, List())

  override def initIfNotExists: Task[Unit] = db(DBIO.seq(fics.schema.createIfNotExists))
