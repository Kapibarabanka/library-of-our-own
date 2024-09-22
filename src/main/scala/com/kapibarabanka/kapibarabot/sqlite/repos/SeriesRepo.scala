package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.{FicType, Rating, Series}
import com.kapibarabanka.kapibarabot.domain.{FicDisplayModel, MyFicStats}
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.{SeriesDoc, SeriesToWorksDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{SeriesTable, SeriesToWorksTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class SeriesRepo(userId: String) extends WithDb(userId):
  private val series        = TableQuery[SeriesTable]
  private val seriesToWorks = TableQuery[SeriesToWorksTable]
  private val works         = WorksRepo(userId)
  private val stats         = StatsRepo(userId)

  def add(s: Series): IO[Throwable, FicDisplayModel] = {
    db(
      DBIO
        .seq(
          series += SeriesDoc.fromModel(s),
          DBIO.sequence(s.works.flatMap(works.getAddingAction)).transactionally,
          seriesToWorks ++= (s.works.indices zip s.works).map((idx, work) => SeriesToWorksDoc(None, s.id, work.id, idx + 1))
        )
        .transactionally
    )
      .flatMap(_ => getById(s.id).map(_.get))
  }

  def getById(id: String): IO[Throwable, Option[FicDisplayModel]] = for {
    docs <- db(series.filter(_.id === id).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToDisplayModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  def patchStats(id: String, stats: MyFicStats): IO[Throwable, FicDisplayModel] = {
    val seriesQuery        = series.filter(_.id === id).map(f => (f.backlog, f.isOnKindle))
    val updateSeriesAction = seriesQuery.update((stats.backlog, stats.isOnKindle))
    for {
      workIds <- db(seriesToWorks.filter(_.seriesId === id).map(doc => doc.workId).result)
      _ <- db(
        DBIO
          .seq(
            updateSeriesAction,
            DBIO.sequence(workIds.map(workId => works.updateStatsFromSeriesAction(workId, stats))).transactionally
          )
          .transactionally
      )
      fic <- getById(id).map(_.get)
    } yield fic
  }

  private def docToDisplayModel(doc: SeriesDoc) = for {
    comments             <- stats.getAllComments(doc.key)
    readDates            <- stats.getReadDates(doc.key)
    workIdsWithPositions <- db(seriesToWorks.filter(_.seriesId === doc.id).map(d => (d.workId, d.positionInSeries)).result)
    workIds              <- ZIO.succeed(workIdsWithPositions.sortBy(_._2).map(_._1))
    works                <- ZIO.collectAll(workIds.map(id => works.getById(id))).map(_.flatten)
  } yield FicDisplayModel(
    id = doc.id,
    ficType = FicType.Series,
    link = doc.link,
    title = doc.title,
    authors = doc.authors.split(", ").toList,
    rating = works.map(_.rating).maxBy(_.id),
    categories = works.flatMap(_.categories).toSet,
    fandoms = works.flatMap(_.fandoms).toSet,
    characters = works.flatMap(_.characters).toSet,
    relationships = works.flatMap(_.relationships).toList.distinct,
    tags = works.flatMap(_.tags).toList.distinct,
    comments = comments,
    readDates = readDates,
    words = doc.words,
    complete = doc.complete,
    stats = MyFicStats(
      read = works.forall(_.stats.read),
      backlog = doc.backlog,
      isOnKindle = doc.isOnKindle,
      quality = if (works.exists(_.stats.quality.isEmpty)) None else Some(works.map(_.stats.quality.get).minBy(_.id)),
      fire = works.exists(_.stats.fire)
    )
  )
