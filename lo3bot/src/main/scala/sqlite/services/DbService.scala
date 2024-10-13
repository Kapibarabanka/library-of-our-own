package kapibarabanka.lo3.bot
package sqlite.services

import sqlite.SqliteError
import zio.IO

class DbService(dbWithPath: String):
  private val db: KapibarabotDb  = KapibarabotDb(dbWithPath)
  val fics: FicService           = FicService(db)
  val details: FicDetailsService = FicDetailsService(db, fics)
  val users: UsersService        = UsersService(db)

  def init: IO[SqliteError, Unit] = db.init
