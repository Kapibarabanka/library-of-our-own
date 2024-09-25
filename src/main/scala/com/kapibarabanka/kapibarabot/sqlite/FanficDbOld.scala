package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDetails, FlatFicModel, UserFicKey, UserFicRecord}
import com.kapibarabanka.kapibarabot.sqlite.repos.{SeriesRepo, UserFicRepo, WorksRepo}
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class FanficDbOld:
  private val works                    = WorksRepo()
  private val series                   = SeriesRepo()
  private val userFicRepo: UserFicRepo = UserFicRepo()

  val allTables: List[MyTable] = List(
    UsersTable,
    WorksTable,
    SeriesTable,
    FicsDetailsTable,
    SeriesToWorksTable,
    FandomsTable,
    WorksToFandomsTable,
    CharactersTable,
    WorksToCharactersTable,
    RelationshipsTable,
    ShipsToCharactersTable,
    WorksToShipsTable,
    TagsTable,
    WorksToTagsTable,
    CommentsTable,
    ReadDatesTable
  )

  def init = for {
    _ <- Sqlite.run(DBIO.sequence(allTables.map(_.createIfNotExists)))
  } yield ()

  def beginWithTestData = {
    for {
      _ <- Sqlite.run(DBIO.sequence(allTables.map(_.dropIfExists)))
      _ <- init
      _ <- works.add(TestData.ratiorine)
      _ <- series.add(TestData.opSeries)
      _ <- userFicRepo.addComment(UserFicKey(TestData.userId1, TestData.angstyZoSan.id, FicType.Work), TestData.comment)
    } yield ()
  }

  def add(work: Work): IO[Throwable, FlatFicModel] = works.add(work)
  def add(s: Series): IO[Throwable, FlatFicModel]  = series.add(s)

  def ficIsInDb(ficId: String, ficType: FicType): IO[Throwable, Boolean] = getFicOption(ficId, ficType).map(_.nonEmpty)

  def getFicOption(ficId: String, ficType: FicType): IO[Throwable, Option[FlatFicModel]] = ficType match
    case models.FicType.Work   => works.getById(ficId)
    case models.FicType.Series => series.getById(ficId)

  def getOrCreateUserFic(key: UserFicKey): IO[Throwable, UserFicRecord] = for {
    details       <- userFicRepo.getOrCreateDetails(key)
    readDatesInfo <- userFicRepo.getReadDatesInfo(key)
    comments      <- userFicRepo.getAllComments(key)
    fic           <- getFicOption(key.ficId, key.ficType).map(_.get)
  } yield UserFicRecord(
    userId = key.userId,
    fic = fic,
    readDatesInfo = readDatesInfo,
    comments = comments,
    details = details
  )

  def patchFicStats(record: UserFicRecord, details: FicDetails): IO[Throwable, UserFicRecord] = for {
    _ <- userFicRepo.patchDetails(record.key, details)
  } yield record.copy(details = details)

  def addComment(record: UserFicRecord, comment: FicComment): IO[Throwable, UserFicRecord] = for {
    _        <- userFicRepo.addComment(record.key, comment)
    comments <- userFicRepo.getAllComments(record.key)
  } yield record.copy(comments = comments)

  def addStartDate(record: UserFicRecord, startDate: String): IO[Throwable, UserFicRecord] = for {
    _     <- userFicRepo.addStartDate(record.key, startDate)
    dates <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  def addFinishDate(record: UserFicRecord, finishDate: String): ZIO[Any, Throwable, UserFicRecord] = for {
    _          <- userFicRepo.addFinishDate(record.key, finishDate)
    details    <- userFicRepo.getOrCreateDetails(record.key)
    newDetails <- ZIO.succeed(details.copy(read = true))
    _          <- if (!details.read) patchFicStats(record, newDetails) else ZIO.unit
    dates      <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(details = newDetails, readDatesInfo = dates)

  def cancelStartedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] = for {
    _     <- userFicRepo.cancelStartedToday(record.key)
    dates <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)

  def cancelFinishedToday(record: UserFicRecord): IO[Throwable, UserFicRecord] = for {
    _     <- userFicRepo.cancelFinishedToday(record.key)
    dates <- userFicRepo.getReadDatesInfo(record.key)
  } yield record.copy(readDatesInfo = dates)
