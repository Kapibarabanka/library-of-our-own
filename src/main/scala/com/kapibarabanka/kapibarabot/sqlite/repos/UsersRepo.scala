package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.services.KapibarabotDb
import com.kapibarabanka.kapibarabot.sqlite.tables.UsersTable
import slick.jdbc.PostgresProfile.api.*
import zio.IO

class UsersRepo(db: KapibarabotDb):
  private val users = TableQuery[UsersTable]

  def getKindleEmail(userId: String): IO[SqliteError, Option[String]] =
    db.run(users.filter(_.chatId === userId).map(_.kindleEmail).result).map(_.flatten.headOption)
