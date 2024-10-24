package kapibarabanka.lo3.api
package sqlite.services


import sqlite.repos.{SeriesRepo, WorksRepo}

import kapibarabanka.lo3.models.ao3.{FicType, Series, Work}
import kapibarabanka.lo3.models.tg.FlatFicModel
import zio.IO

case class FicService(db: KapibarabotDb):
  private val works  = WorksRepo(db)
  private val series = SeriesRepo(db, works)

  def add(work: Work): IO[String, FlatFicModel] = works.add(work)

  def add(s: Series): IO[String, FlatFicModel] = series.add(s)

  def isInDb(ficId: String, ficType: FicType): IO[String, Boolean] = getByIdOption(ficId, ficType).map(_.nonEmpty)

  def getByIdOption(ficId: String, ficType: FicType): IO[String, Option[FlatFicModel]] = ficType match
    case FicType.Work   => works.getById(ficId)
    case FicType.Series => series.getById(ficId)
