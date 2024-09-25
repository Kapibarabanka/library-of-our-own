package com.kapibarabanka.kapibarabot.services

import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.FlatFicModel
import zio.{IO, ZIO}

trait FicService:
  def add(work: Work): IO[Throwable, FlatFicModel]
  def add(s: Series): IO[Throwable, FlatFicModel]
  def ficIsInDb(ficId: String, ficType: FicType): IO[Throwable, Boolean]
  def getFicOption(ficId: String, ficType: FicType): IO[Throwable, Option[FlatFicModel]]

object FicService:
  def add(work: Work): ZIO[FicService, Throwable, FlatFicModel] =
    ZIO.serviceWithZIO[FicService](_.add(work))

  def add(s: Series): ZIO[FicService, Throwable, FlatFicModel] =
    ZIO.serviceWithZIO[FicService](_.add(s))

  def ficIsInDb(ficId: String, ficType: FicType): ZIO[FicService, Throwable, Boolean] =
    ZIO.serviceWithZIO[FicService](_.ficIsInDb(ficId, ficType))

  def getFicOption(ficId: String, ficType: FicType): ZIO[FicService, Throwable, Option[FlatFicModel]] =
    ZIO.serviceWithZIO[FicService](_.getFicOption(ficId, ficType))
