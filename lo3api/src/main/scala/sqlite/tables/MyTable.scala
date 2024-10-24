package kapibarabanka.lo3.api
package sqlite.tables

import slick.jdbc.PostgresProfile.api.*

trait MyTable:
  val name: String
  val keyField: String
  def createIfNotExists: DBIO[Int] | DBIOAction[Unit, slick.dbio.NoStream, slick.dbio.Effect.Schema]
  def dropIfExists: DBIO[Int] = sqlu"DROP TABLE IF EXISTS #$name"
