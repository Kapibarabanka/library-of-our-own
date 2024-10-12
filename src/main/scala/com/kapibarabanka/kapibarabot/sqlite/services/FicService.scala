package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.ao3scrapper.domain.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.FlatFicModel
import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.repos.{SeriesRepo, WorksRepo}
import zio.{IO, ZIO, ZLayer}

case class FicService(db: KapibarabotDb):
  private val works  = WorksRepo(db)
  private val series = SeriesRepo(db, works)

  def add(work: Work): IO[SqliteError, FlatFicModel] = works.add(work)

  def add(s: Series): IO[SqliteError, FlatFicModel] = series.add(s)

  def isInDb(ficId: String, ficType: FicType): IO[SqliteError, Boolean] = getByIdOption(ficId, ficType).map(_.nonEmpty)

  def getByIdOption(ficId: String, ficType: FicType): IO[SqliteError, Option[FlatFicModel]] = ficType match
    case FicType.Work   => works.getById(ficId)
    case FicType.Series => series.getById(ficId)
