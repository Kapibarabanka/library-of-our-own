package kapibarabanka.lo3.bot
package sqlite.repos

import sqlite.SqliteError
import sqlite.services.KapibarabotDb
import sqlite.tables.UsersTable
import slick.jdbc.PostgresProfile.api.*
import zio.IO

class UsersRepo(db: KapibarabotDb):
  private val users = TableQuery[UsersTable]

  def getAllUserIds: IO[SqliteError, Seq[String]] = db.run(users.map(_.chatId).result)

  def addUser(userId: String, userName: Option[String]): IO[SqliteError, Unit] =
    db.run(
      sqlu"INSERT OR IGNORE INTO #${UsersTable.name} (chatId, userName) VALUES (#$userId, ${userName.fold("NULL")(l => l)})"
    ).unit

  def getKindleEmail(userId: String): IO[SqliteError, Option[String]] =
    db.run(users.filter(_.chatId === userId).map(_.kindleEmail).result).map(_.flatten.headOption)

  def setKindleEmail(userId: String, email: String): IO[SqliteError, Unit] =
    db.run(users.filter(_.chatId === userId).map(_.kindleEmail).update(Some(email))).unit
