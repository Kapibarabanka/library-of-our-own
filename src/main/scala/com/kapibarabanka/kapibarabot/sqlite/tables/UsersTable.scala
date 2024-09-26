package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.UserDoc
import slick.jdbc.PostgresProfile.api.*

class UsersTable(tag: Tag) extends Table[UserDoc](tag, UsersTable.name):
  def chatId      = column[String]("chatId", O.PrimaryKey, O.Unique)
  def kindleEmail = column[Option[String]]("kindleEmail")

  def * = (chatId, kindleEmail).mapTo[UserDoc]

object UsersTable extends MyTable:
  override val name: String     = "Users"
  override val keyField: String = "chatId"

  def createIfNotExists = TableQuery[UsersTable].schema.createIfNotExists