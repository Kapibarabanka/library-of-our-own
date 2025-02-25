package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.{SeriesDoc, SeriesToWorksDoc}
import sqlite.services.Lo3Db
import sqlite.tables.{FicsDetailsTable, SeriesTable, SeriesToWorksTable}

import kapibarabanka.lo3.common.models.ao3.{FicType, Series}
import kapibarabanka.lo3.common.models.domain.{DbError, FicDetails, FlatFicModel}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class SeriesRepo(db: Lo3Db, worksRepo: WorksRepo):
  private val series        = TableQuery[SeriesTable]
  private val seriesToWorks = TableQuery[SeriesToWorksTable]
  private val ficDetails    = TableQuery[FicsDetailsTable]

  def exists(id: String): IO[DbError, Boolean] =
    db.run(series.filter(_.id === id).result).map(docs => docs.headOption.nonEmpty)

  def add(s: Series): IO[DbError, FlatFicModel] = {
    for {
      _ <- db
        .run(
          DBIO
            .seq(
              series += SeriesDoc.fromModel(s),
              DBIO.sequence(s.unsavedWorks.flatMap(w => worksRepo.getAddingAction(w, true))).transactionally,
              seriesToWorks ++= (s.workIds.indices zip s.workIds).map((idx, workId) =>
                SeriesToWorksDoc(None, s.id, workId, idx + 1)
              )
            )
            .transactionally
        )
      flatFic <- getById(s.id).map(_.get)
    } yield flatFic
  }

  def update(s: Series): IO[DbError, FlatFicModel] = for {
    maybeExisting <- getById(s.id)
    updated <- maybeExisting match
      case None => add(s)
      case Some(_) =>
        for {
          _ <- db
            .run(
              DBIO
                .seq(
                  seriesToWorks.filter(_.seriesId === s.id).delete,
                  series.filter(_.id === s.id).update(SeriesDoc.fromModel(s)),
                  DBIO.sequence(s.unsavedWorks.flatMap(w => worksRepo.getAddingAction(w, true))).transactionally,
                  seriesToWorks ++= (s.workIds.indices zip s.workIds).map((idx, workId) =>
                    SeriesToWorksDoc(None, s.id, workId, idx + 1)
                  )
                )
                .transactionally
            )
          flatFic <- getById(s.id).map(_.get)
        } yield flatFic
  } yield updated

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

  def getAllForUser(userId: String, allWorks: List[FlatFicModel]): IO[DbError, List[(FicDetails, FlatFicModel, List[String])]] =
    for {
      seriesWithDetails <- db.run((for {
        details <- ficDetails if (details.ficIsSeries === true && details.userId === userId)
        s       <- series if (s.id === details.ficId)
      } yield (details, s)).result)
      result <- ZIO.collectAll(
        seriesWithDetails.map((detailsDoc, seriesDoc) =>
          for {
            workIdsWithPositions <- db.run(
              seriesToWorks.filter(_.seriesId === seriesDoc.id).map(d => (d.workId, d.positionInSeries)).result
            )
            workIds     <- ZIO.succeed(workIdsWithPositions.sortBy(_._2).map(_._1).toSet)
            seriesWorks <- ZIO.succeed(allWorks.filter(w => workIds.contains(w.id)))
            seriesFic   <- docToModel(seriesDoc, Some(seriesWorks))
          } yield (detailsDoc.toModel, seriesFic, workIds.toList)
        )
      )
    } yield result.toList

  def getAll: IO[DbError, List[FlatFicModel]] = for {
    docs   <- db.run(series.result)
    models <- ZIO.collectAll(docs.map(docToModel(_)))
  } yield models.toList

  private def docToModel(doc: SeriesDoc, maybeWorks: Option[Seq[FlatFicModel]] = None) =
    val seriesWorks = maybeWorks match
      case Some(value) => ZIO.succeed(value)
      case None =>
        for {
          workIdsWithPositions <- db.run(
            seriesToWorks.filter(_.seriesId === doc.id).map(d => (d.workId, d.positionInSeries)).result
          )
          workIds <- ZIO.succeed(workIdsWithPositions.sortBy(_._2).map(_._1))
          works   <- ZIO.collectAll(workIds.map(id => worksRepo.getById(id))).map(_.flatten)
        } yield works
    seriesWorks.map(works =>
      FlatFicModel(
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
        words = works.map(_.words).sum,
        complete = doc.complete,
        partsWritten = works.length
      )
    )
