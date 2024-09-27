package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.ao3scrapper.domain.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.FlatFicModel
import com.kapibarabanka.kapibarabot.sqlite.repos.{SeriesRepo, WorksRepo}
import zio.{IO, ZIO, ZLayer}

trait FicService:
  def add(work: Work): IO[Throwable, FlatFicModel]
  def add(s: Series): IO[Throwable, FlatFicModel]
  def ficIsInDb(ficId: String, ficType: FicType): IO[Throwable, Boolean]
  def getFicOption(ficId: String, ficType: FicType): IO[Throwable, Option[FlatFicModel]]

case class FicServiceImpl(db: KapibarabotDb) extends FicService:
  private val works  = WorksRepo(db)
  private val series = SeriesRepo(db, works)

  override def add(work: Work): IO[Throwable, FlatFicModel] = works.add(work)

  override def add(s: Series): IO[Throwable, FlatFicModel] = series.add(s)

  override def ficIsInDb(ficId: String, ficType: FicType): IO[Throwable, Boolean] = getFicOption(ficId, ficType).map(_.nonEmpty)

  override def getFicOption(ficId: String, ficType: FicType): IO[Throwable, Option[FlatFicModel]] = ficType match
    case FicType.Work   => works.getById(ficId)
    case FicType.Series => series.getById(ficId)

object FicServiceImpl:
  val layer: ZLayer[KapibarabotDb, Nothing, FicServiceImpl] = ZLayer {
    for {
      db <- ZIO.service[KapibarabotDb]
    } yield FicServiceImpl(db)
  }

object FicService:
  def add(work: Work): ZIO[FicService, Throwable, FlatFicModel] =
    ZIO.serviceWithZIO[FicService](_.add(work))

  def add(s: Series): ZIO[FicService, Throwable, FlatFicModel] =
    ZIO.serviceWithZIO[FicService](_.add(s))

  def ficIsInDb(ficId: String, ficType: FicType): ZIO[FicService, Throwable, Boolean] =
    ZIO.serviceWithZIO[FicService](_.ficIsInDb(ficId, ficType))

  def getFicOption(ficId: String, ficType: FicType): ZIO[FicService, Throwable, Option[FlatFicModel]] =
    ZIO.serviceWithZIO[FicService](_.getFicOption(ficId, ficType))
