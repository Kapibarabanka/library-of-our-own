package kapibarabanka.lo3.api
package sqlite.services

import sqlite.repos.*

import kapibarabanka.lo3.common.AppConfig

object Lo3Data:
  private val dbWithPath = s"${AppConfig.dbPath}${AppConfig.dbName}"

  private val db: Lo3Db = Lo3Db(dbWithPath)

  def init = db.init

  val tags      = TagsRepo(db)
  val works     = WorksRepo(db, tags)
  val series    = SeriesRepo(db, works)
  val details   = FicDetailsRepo(db)
  val notes     = NotesRepo(db)
  val readDates = ReadDatesRepo(db)
  val users     = UsersRepo(db)
  val fics      = FicsRepo(db)
