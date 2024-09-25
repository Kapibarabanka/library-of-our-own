package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.{FicType, Rating, Series}
import com.kapibarabanka.kapibarabot.domain.{FicDetails, FlatFicModel}
import com.kapibarabanka.kapibarabot.sqlite.Sqlite
import com.kapibarabanka.kapibarabot.sqlite.docs.{SeriesDoc, SeriesToWorksDoc}
import com.kapibarabanka.kapibarabot.sqlite.tables.{SeriesTable, SeriesToWorksTable}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class SeriesRepo:
  private val series        = TableQuery[SeriesTable]
  private val seriesToWorks = TableQuery[SeriesToWorksTable]
  private val works         = WorksRepo()

  def add(s: Series): IO[Throwable, FlatFicModel] = {
    Sqlite
      .run(
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

  def getById(id: String): IO[Throwable, Option[FlatFicModel]] = for {
    docs <- Sqlite.run(series.filter(_.id === id).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  def getAll: IO[Throwable, List[FlatFicModel]] = for {
    docs   <- Sqlite.run(series.result)
    models <- ZIO.collectAll(docs.map(docToModel))
  } yield models.toList

  private def docToModel(doc: SeriesDoc) = for {
    workIdsWithPositions <- Sqlite.run(
      seriesToWorks.filter(_.seriesId === doc.id).map(d => (d.workId, d.positionInSeries)).result
    )
    workIds <- ZIO.succeed(workIdsWithPositions.sortBy(_._2).map(_._1))
    works   <- ZIO.collectAll(workIds.map(id => works.getById(id))).map(_.flatten)
  } yield FlatFicModel(
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
    words = doc.words,
    complete = doc.complete
  )
