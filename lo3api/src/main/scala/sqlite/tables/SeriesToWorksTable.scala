package kapibarabanka.lo3.api
package sqlite.tables

import sqlite.docs.SeriesToWorksDoc

import slick.jdbc.PostgresProfile.api.*

class SeriesToWorksTable(tag: Tag) extends Table[SeriesToWorksDoc](tag, SeriesToWorksTable.name):
  def id               = column[Int]("id", O.PrimaryKey, O.Unique)
  def seriesId         = column[String]("seriesId")
  def workId           = column[String]("workId")
  def positionInSeries = column[Int]("positionInSeries")

  def * = (id.?, seriesId, workId, positionInSeries).mapTo[SeriesToWorksDoc]

object SeriesToWorksTable extends MyTable:
  override val name: String     = "SeriesToWorks"
  override val keyField: String = "id"

  def createIfNotExists = sqlu"""
      CREATE TABLE IF NOT EXISTS "#$name" (
      "id"	INTEGER NOT NULL UNIQUE,
      "seriesId"	TEXT NOT NULL,
      "workId"	TEXT NOT NULL,
      "positionInSeries"	INTEGER NOT NULL,
      PRIMARY KEY("id"),
      FOREIGN KEY("seriesId") REFERENCES "#${SeriesTable.name}"("#${SeriesTable.keyField}") ON UPDATE CASCADE ON DELETE CASCADE);
      FOREIGN KEY("workId") REFERENCES "#${WorksTable.name}"("#${WorksTable.keyField}") ON UPDATE CASCADE ON DELETE CASCADE);
      """
