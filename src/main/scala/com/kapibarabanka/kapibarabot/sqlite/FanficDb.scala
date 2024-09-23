package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel, FicKey, MyFicStats}
import com.kapibarabanka.kapibarabot.sqlite.repos.{SeriesRepo, StatsRepo, WorksRepo}
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import scalaz.Scalaz.ToIdOps
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class FanficDb(userId: String) extends WithDb(userId):
  private val works            = WorksRepo(userId)
  private val series           = SeriesRepo(userId)
  private val stats: StatsRepo = StatsRepo(userId)

  val allTables: List[MyTable] = List(
    WorksTable,
    SeriesTable,
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
    _ <- db(DBIO.sequence(allTables.map(_.createIfNotExists)))
  } yield ()

  def beginWithTestData = {
    for {
      _ <- db(DBIO.sequence(allTables.map(_.dropIfExists)))
      _ <- init
      _ <- works.add(TestData.ratiorine)
      _ <- series.add(TestData.opSeries)
      _ <- stats.addComment(FicKey.fromWork(TestData.angstyZoSan), TestData.comment)
      _ <- works.patchStats(TestData.ratiorine.id, TestData.readStats)
    } yield ()
  }

  def add(work: Work): IO[Throwable, FicDisplayModel] = works.add(work)
  def add(s: Series): IO[Throwable, FicDisplayModel]  = series.add(s)

  def getFicOption(key: FicKey): IO[Throwable, Option[FicDisplayModel]] = key.ficType match
    case models.FicType.Work   => works.getById(key.ficId)
    case models.FicType.Series => series.getById(key.ficId)

  def getWorkOption(workId: String): IO[Throwable, Option[FicDisplayModel]]     = works.getById(workId)
  def getSeriesOption(seriesId: String): IO[Throwable, Option[FicDisplayModel]] = series.getById(seriesId)

  def getAllWorks: IO[Throwable, List[FicDisplayModel]] = works.getAll

  def patchFicStats(key: FicKey, stats: MyFicStats): IO[Throwable, FicDisplayModel] = key.ficType match
    case models.FicType.Work   => works.patchStats(key.ficId, stats)
    case models.FicType.Series => series.patchStats(key.ficId, stats)

  def addComment(key: FicKey, comment: FicComment): IO[Throwable, FicDisplayModel] =
    stats.addComment(key, comment) |> returnPatchedFic(key)

  def addStartDate(key: FicKey, startDate: String): IO[Throwable, FicDisplayModel] =
    stats.addStartDate(key, startDate) |> returnPatchedFic(key)

  def addFinishDate(key: FicKey, finishDate: String): ZIO[Any, Throwable, FicDisplayModel] = (for {
    _   <- stats.addFinishDate(key, finishDate)
    fic <- getFicOption(key).map(_.get)
    _   <- patchFicStats(key, fic.stats.copy(read = true))
  } yield ()) |> returnPatchedFic(key)

  def cancelStartedToday(key: FicKey): IO[Throwable, FicDisplayModel]  = stats.cancelStartedToday(key) |> returnPatchedFic(key)
  def cancelFinishedToday(key: FicKey): IO[Throwable, FicDisplayModel] = stats.cancelFinishedToday(key) |> returnPatchedFic(key)

  def returnPatchedFic(key: FicKey)(action: IO[Throwable, Any]): IO[Throwable, FicDisplayModel] =
    action.flatMap(_ => getFicOption(key).map(_.get))
