package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.ao3scrapper.domain.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.FlatFicModel
import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.repos.{SeriesRepo, WorksRepo}
import zio.{IO, ZIO, ZLayer}

trait FicService:
  def add(work: Work): IO[SqliteError, FlatFicModel]
  def add(s: Series): IO[SqliteError, FlatFicModel]
  def ficIsInDb(ficId: String, ficType: FicType): IO[SqliteError, Boolean]
  def getFicOption(ficId: String, ficType: FicType): IO[SqliteError, Option[FlatFicModel]]

case class FicServiceImpl(db: KapibarabotDb) extends FicService:
  private val works  = WorksRepo(db)
  private val series = SeriesRepo(db, works)

  override def add(work: Work): IO[SqliteError, FlatFicModel] = works.add(work)

  override def add(s: Series): IO[SqliteError, FlatFicModel] = series.add(s)

  override def ficIsInDb(ficId: String, ficType: FicType): IO[SqliteError, Boolean] = getFicOption(ficId, ficType).map(_.nonEmpty)

  override def getFicOption(ficId: String, ficType: FicType): IO[SqliteError, Option[FlatFicModel]] = ficType match
    case FicType.Work   => works.getById(ficId)
    case FicType.Series => series.getById(ficId)

object FicServiceImpl:
  val layer: ZLayer[KapibarabotDb, Nothing, FicServiceImpl] = ZLayer {
    for {
      db <- ZIO.service[KapibarabotDb]
    } yield FicServiceImpl(db)
  }

object FicService:
  def add(work: Work): ZIO[FicService, SqliteError, FlatFicModel] =
    ZIO.serviceWithZIO[FicService](_.add(work))

  def add(s: Series): ZIO[FicService, SqliteError, FlatFicModel] =
    ZIO.serviceWithZIO[FicService](_.add(s))

  def ficIsInDb(ficId: String, ficType: FicType): ZIO[FicService, SqliteError, Boolean] =
    ZIO.serviceWithZIO[FicService](_.ficIsInDb(ficId, ficType))

  def getFicOption(ficId: String, ficType: FicType): ZIO[FicService, Throwable, Option[FlatFicModel]] =
    ZIO.serviceWithZIO[FicService](_.getFicOption(ficId, ficType))
