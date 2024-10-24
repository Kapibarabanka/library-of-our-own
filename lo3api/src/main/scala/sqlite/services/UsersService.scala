package kapibarabanka.lo3.api
package sqlite.services

import sqlite.SqliteError
import sqlite.repos.UsersRepo

import zio.IO

case class UsersService(db: KapibarabotDb):
  private val usersRepo = UsersRepo(db)

  def getAllIds: IO[SqliteError, List[String]] = usersRepo.getAllUserIds

  def addUser(userId: String, userName: Option[String]): IO[SqliteError, Unit] = usersRepo.addUser(userId, userName)

  def getKindleEmail(userId: String): IO[SqliteError, Option[String]] = usersRepo.getKindleEmail(userId)

  def setKindleEmail(userId: String, email: String): IO[SqliteError, Unit] = usersRepo.setKindleEmail(userId, email)
