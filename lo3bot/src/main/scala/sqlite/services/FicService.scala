package kapibarabanka.lo3.bot
package sqlite.services

import kapibarabanka.lo3.models.ao3.{FicType, Series, Work}
import kapibarabanka.lo3.models.tg.FlatFicModel
import sqlite.SqliteError
import sqlite.repos.{SeriesRepo, WorksRepo}

import zio.IO

case class FicService(db: KapibarabotDb):
  private val works  = WorksRepo(db)
  private val series = SeriesRepo(db, works)

  def add(work: Work): IO[SqliteError, FlatFicModel] = works.add(work)

  def add(s: Series): IO[SqliteError, FlatFicModel] = series.add(s)

  def isInDb(ficId: String, ficType: FicType): IO[SqliteError, Boolean] = getByIdOption(ficId, ficType).map(_.nonEmpty)

  def getByIdOption(ficId: String, ficType: FicType): IO[SqliteError, Option[FlatFicModel]] = ficType match
    case FicType.Work   => works.getById(ficId)
    case FicType.Series => series.getById(ficId)
