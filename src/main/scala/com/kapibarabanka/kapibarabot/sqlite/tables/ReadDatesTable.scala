package com.kapibarabanka.kapibarabot.sqlite.tables

import com.kapibarabanka.kapibarabot.sqlite.docs.ReadDatesDoc
import slick.jdbc.PostgresProfile.api.*

class ReadDatesTable(tag: Tag) extends Table[ReadDatesDoc](tag, ReadDatesTable.name):
  def id          = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId       = column[String]("ficId")
  def ficIsSeries = column[Boolean]("ficIsSeries")
  def startDate   = column[Option[String]]("startDate")
  def endDate     = column[Option[String]]("endDate")

  def * = (id.?, ficId, ficIsSeries, startDate, endDate).mapTo[ReadDatesDoc]

object ReadDatesTable extends MyTable:
  override val name: String     = "ReadDates"
  override val keyField: String = "id"

  def createIfNotExists: DBIO[Int] | DBIOAction[Unit, NoStream, Effect.Schema] =
    TableQuery[ReadDatesTable].schema.createIfNotExists
