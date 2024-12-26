package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.{SeriesDoc, SeriesToWorksDoc}
import sqlite.services.Lo3Db
import sqlite.tables.{FicsDetailsTable, SeriesTable, SeriesToWorksTable}

import kapibarabanka.lo3.common.models.ao3.{FicType, Series}
import kapibarabanka.lo3.common.models.domain.{DbError, FicDetails, FlatFicModel}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class SeriesRepo(db: Lo3Db, works: WorksRepo):
  private val series        = TableQuery[SeriesTable]
  private val seriesToWorks = TableQuery[SeriesToWorksTable]
  private val ficDetails    = TableQuery[FicsDetailsTable]

  def exists(id: String): IO[DbError, Boolean] =
    db.run(series.filter(_.id === id).result).map(docs => docs.headOption.nonEmpty)

  def add(s: Series): IO[DbError, FlatFicModel] = {
    for {
      maybeWorks <- ZIO.collectAll(s.works.map(w => works.exists(w.id).map(exists => if (exists) None else Some(w))))
      newWorks   <- ZIO.succeed(maybeWorks.flatten)
      _ <- db
        .run(
          DBIO
            .seq(
              series += SeriesDoc.fromModel(s),
              DBIO.sequence(newWorks.flatMap(works.getAddingAction)).transactionally,
              seriesToWorks ++= (s.works.indices zip s.works).map((idx, work) => SeriesToWorksDoc(None, s.id, work.id, idx + 1))
            )
            .transactionally
        )
      flatFic <- getById(s.id).map(_.get)
    } yield flatFic
  }

  def workIds(id: String) = for {
    workIdsWithPositions <- db.run(seriesToWorks.filter(_.seriesId === id).map(d => (d.workId, d.positionInSeries)).result)
    workIds              <- ZIO.succeed(workIdsWithPositions.sortBy(_._2).map(_._1))
  } yield workIds.toList

  def title(id: String) = db.run(series.filter(_.id === id).map(_.title).result).map(_.headOption)

  def getById(id: String): IO[DbError, Option[FlatFicModel]] = for {
    docs <- db.run(series.filter(_.id === id).result)
    maybeDisplayModel <- docs.headOption match
      case Some(doc) => docToModel(doc).map(Some(_))
      case None      => ZIO.succeed(None)
  } yield maybeDisplayModel

  def getAllForUser(userId: String): IO[DbError, List[(FicDetails, FlatFicModel)]] = db
    .run(
      (for {
        details <- ficDetails if (details.ficIsSeries === true && details.userId === userId)
        s       <- series if (s.id === details.ficId)
        // doctOModel can be modified to return all works together with series
      } yield (details, s)).result
    )
    .flatMap(seq =>
      ZIO.collectAll(seq.map((detailsDoc, seriesDoc) => docToModel(seriesDoc).map(work => (detailsDoc.toModel, work))).toList)
    )

  def getAll: IO[DbError, List[FlatFicModel]] = for {
    docs   <- db.run(series.result)
    models <- ZIO.collectAll(docs.map(docToModel))
  } yield models.toList

  private def docToModel(doc: SeriesDoc) = for {
    workIdsWithPositions <- db.run(
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
    warnings = works.flatMap(_.warnings).toSet,
    fandoms = works.flatMap(_.fandoms).toSet,
    characters = works.flatMap(_.characters).toSet,
    relationships = works.flatMap(_.relationships).toList.distinct,
    tags = works.flatMap(_.tags).toList.distinct,
    words = doc.words,
    complete = doc.complete
  )
