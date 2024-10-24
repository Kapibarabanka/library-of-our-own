package kapibarabanka.lo3.api
package sqlite.services


import sqlite.repos.UsersRepo

import zio.IO

case class UsersService(db: KapibarabotDb):
  private val usersRepo = UsersRepo(db)

  def getAllIds: IO[String, List[String]] = usersRepo.getAllUserIds

  def addUser(userId: String, userName: Option[String]): IO[String, Unit] = usersRepo.addUser(userId, userName)

  def getKindleEmail(userId: String): IO[String, Option[String]] = usersRepo.getKindleEmail(userId)

  def setKindleEmail(userId: String, email: String): IO[String, Unit] = usersRepo.setKindleEmail(userId, email)
