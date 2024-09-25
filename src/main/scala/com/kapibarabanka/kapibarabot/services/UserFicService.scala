package com.kapibarabanka.kapibarabot.services

import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDetails, UserFicKey, UserFicRecord}
import zio.{IO, ZIO}

trait UserFicService:
  def getOrCreateUserFic(key: UserFicKey): IO[Throwable, UserFicRecord]
  def patchFicStats(record: UserFicRecord, details: FicDetails): IO[Throwable, UserFicRecord]
  def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord]
  def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord]
  def addFinishDate(record: UserFicRecord, finishDate: String): IO[Throwable, UserFicRecord]
  def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord]
  def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord]

object UserFicService:
  def getOrCreateUserFic(key: UserFicKey): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.getOrCreateUserFic(key))

  def patchFicStats(record: UserFicRecord, details: FicDetails): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.patchFicStats(record, details))

  def addComment(record: UserFicRecord, comment: FicComment): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.addComment(record, comment))

  def addStartDate(record: UserFicRecord, startDate: String): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.addStartDate(record, startDate))

  def addFinishDate(record: UserFicRecord, finishDate: String): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.addFinishDate(record, finishDate))

  def cancelStartedToday(record: UserFicRecord): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.cancelStartedToday(record))

  def cancelFinishedToday(record: UserFicRecord): ZIO[UserFicService, Throwable, UserFicRecord] =
    ZIO.serviceWithZIO[UserFicService](_.cancelFinishedToday(record))
