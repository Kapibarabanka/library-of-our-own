package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.domain.MyFicModel
import com.kapibarabanka.kapibarabot.sqlite.*
import com.kapibarabanka.kapibarabot.sqlite.docs.*
import com.kapibarabanka.kapibarabot.sqlite.tables.{CharactersTable, FicsTable, FicsToCharactersTable, FicsToTagsTable}
import slick.jdbc.PostgresProfile.api.*
import zio.ZIO

class FicsRepo(userId: String) extends WithDb(userId):
  private val fics           = TableQuery[FicsTable]
  private val tagsRepo       = TagsRepo(userId)
  private val charactersRepo = CharactersRepo(userId)
  private val fandomsRepo    = FandomsRepo(userId)

  def add(fic: MyFicModel): ZIO[Any, Throwable, Unit] = for {
    _ <- db(fics += FicDoc.fromModel(fic))
    _ <- fandomsRepo.addAndLink(fic.fandoms, fic.id)
    _ <- tagsRepo.addAndLink(fic.tags, fic.id)
    _ <- charactersRepo.addAndLinkToFic(fic.characters, fic.id)
  } yield ()

  def getById(ficId: String): ZIO[Any, Throwable, MyFicModel] = for {
    fandoms    <- fandomsRepo.getFicFandoms(ficId)
    characters <- charactersRepo.getFicCharacters(ficId)
    tags       <- tagsRepo.getFicTags(ficId)
    ficDoc     <- db(fics.filter(f => f.id === ficId).result).map(_.head)
  } yield ficDoc.toModel(fandoms, characters, List(), tags)
