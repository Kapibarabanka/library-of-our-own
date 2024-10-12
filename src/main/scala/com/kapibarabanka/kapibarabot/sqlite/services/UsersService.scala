package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.kapibarabot.domain.UserFicRecord
import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.repos.{FicDetailsRepo, UsersRepo}
import zio.{IO, ZIO}

case class UsersService(db: KapibarabotDb):
  private val usersRepo   = UsersRepo(db)
  
  def getKindleEmail(userId: String): IO[SqliteError, Option[String]] = usersRepo.getKindleEmail(userId)
