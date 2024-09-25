package com.kapibarabanka.kapibarabot.services

import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.*
import com.kapibarabanka.kapibarabot.sqlite.KapibarabotDb
import com.kapibarabanka.kapibarabot.sqlite.repos.*
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO, ZLayer}

case class FicServiceImpl(db: KapibarabotDb) extends FicService:
  private val works  = WorksRepo()
  private val series = SeriesRepo()

  override def add(work: Work): IO[Throwable, FlatFicModel] = works.add(work)

  override def add(s: Series): IO[Throwable, FlatFicModel] = series.add(s)

  override def ficIsInDb(ficId: String, ficType: FicType): IO[Throwable, Boolean] = getFicOption(ficId, ficType).map(_.nonEmpty)

  override def getFicOption(ficId: String, ficType: FicType): IO[Throwable, Option[FlatFicModel]] = ficType match
    case FicType.Work   => works.getById(ficId)
    case FicType.Series => series.getById(ficId)

object FicServiceImpl:
  def layer(db: KapibarabotDb): ZLayer[Any, Nothing, FicServiceImpl] = ZLayer {
    ZIO.succeed(FicServiceImpl(db))
  }
