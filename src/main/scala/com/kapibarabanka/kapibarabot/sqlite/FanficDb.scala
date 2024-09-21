package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.ao3scrapper.models
import com.kapibarabanka.ao3scrapper.models.{FicType, Series, Work}
import com.kapibarabanka.kapibarabot.domain.{FicComment, FicDisplayModel, MyFicStats}
import com.kapibarabanka.kapibarabot.sqlite.repos.{CommentsRepo, SeriesRepo, WorksRepo}
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import slick.jdbc.PostgresProfile.api.*
import zio.IO

class FanficDb(userId: String) extends WithDb(userId):
  private val works    = WorksRepo(userId)
  private val series   = SeriesRepo(userId)
  private val comments = CommentsRepo(userId)

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
    CommentsTable
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
      _ <- comments.add(TestData.angstyZoSan.id, false, TestData.comment)
      _ <- works.patchStats(TestData.ratiorine.id, TestData.readStats)
    } yield ()
  }

  def add(work: Work): IO[Throwable, FicDisplayModel] = works.add(work)
  def add(s: Series): IO[Throwable, FicDisplayModel]  = series.add(s)

  def getFicOption(id: String, ficType: FicType): IO[Throwable, Option[FicDisplayModel]] = ficType match
    case models.FicType.Work   => works.getById(id)
    case models.FicType.Series => series.getById(id)

  def getWorkOption(workId: String): IO[Throwable, Option[FicDisplayModel]]     = works.getById(workId)
  def getSeriesOption(seriesId: String): IO[Throwable, Option[FicDisplayModel]] = series.getById(seriesId)

  def patchFicStats(ficId: String, ficType: FicType, stats: MyFicStats): IO[Throwable, FicDisplayModel] = ficType match
    case models.FicType.Work   => works.patchStats(ficId, stats)
    case models.FicType.Series => series.patchStats(ficId, stats)

  def addComment(ficId: String, ficType: FicType, comment: FicComment): IO[Throwable, Unit] =
    comments.add(ficId, ficType == FicType.Series, comment)
