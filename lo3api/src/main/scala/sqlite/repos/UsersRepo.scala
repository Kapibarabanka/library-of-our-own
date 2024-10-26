package kapibarabanka.lo3.api
package sqlite.repos


import sqlite.services.Lo3Db
import sqlite.tables.UsersTable

import slick.jdbc.PostgresProfile.api.*
import zio.IO

class UsersRepo(db: Lo3Db):
  private val users = TableQuery[UsersTable]

  def getAllIds: IO[String, List[String]] = db.run(users.map(_.chatId).result).map(_.toList)

  def addUser(userId: String, userName: Option[String]): IO[String, Unit] =
    db.run(
      sqlu"INSERT OR IGNORE INTO #${UsersTable.name} (chatId, userName) VALUES (#$userId, ${userName.fold("NULL")(l => l)})"
    ).unit

  def getKindleEmail(userId: String): IO[String, Option[String]] =
    db.run(users.filter(_.chatId === userId).map(_.kindleEmail).result).map(_.flatten.headOption)

  def setKindleEmail(userId: String, email: String): IO[String, Unit] =
    db.run(users.filter(_.chatId === userId).map(_.kindleEmail).update(Some(email))).unit
