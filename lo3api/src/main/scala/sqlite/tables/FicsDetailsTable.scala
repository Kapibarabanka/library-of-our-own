package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.FicDetailsDoc

import slick.jdbc.PostgresProfile.api.*

class FicsDetailsTable(tag: Tag) extends Table[FicDetailsDoc](tag, FicsDetailsTable.name):
  def id          = column[Int]("id", O.PrimaryKey, O.Unique)
  def userId      = column[String]("userId")
  def ficId       = column[String]("ficId")
  def ficIsSeries = column[Boolean]("ficIsSeries")

  def backlog       = column[Boolean]("backlog")
  def isOnKindle    = column[Boolean]("isOnKindle")
  def impression    = column[Option[String]]("impression")
  def fire          = column[Boolean]("fire")
  def recordCreated = column[String]("recordCreated")

  def * = (id.?, userId, ficId, ficIsSeries, backlog, isOnKindle, impression, fire, recordCreated).mapTo[FicDetailsDoc]

object FicsDetailsTable extends MyTable:
  override val name: String     = "FicsDetails"
  override val keyField: String = "id"

  def createIfNotExists = TableQuery[FicsDetailsTable].schema.createIfNotExists
