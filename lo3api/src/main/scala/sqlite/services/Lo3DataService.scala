package kapibarabanka.lo3.api
package sqlite.services

import sqlite.repos.*

case class Lo3DataService(dbWithPath: String):
  private val db: Lo3Db = Lo3Db(dbWithPath)

  def init = db.init

  val tags      = TagsRepo(db)
  val works     = WorksRepo(db, tags)
  val series    = SeriesRepo(db, works)
  val details   = FicDetailsRepo(db)
  val comments  = CommentsRepo(db)
  val readDates = ReadDatesRepo(db)
  val users     = UsersRepo(db)
