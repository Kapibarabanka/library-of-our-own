package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.{FicDetailsDoc, SeriesDoc}
import sqlite.services.Lo3Db
import sqlite.tables.*

import kapibarabanka.lo3.common.models.ao3.{FicType, Rating}
import kapibarabanka.lo3.common.models.domain.*
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class FicsRepo(db: Lo3Db):
  private val works             = TableQuery[WorksTable]
  private val worksToTags       = TableQuery[WorksToTagsTable]
  private val worksToFandoms    = TableQuery[WorksToFandomsTable]
  private val worksToCharacters = TableQuery[WorksToCharactersTable]
  private val worksToShips      = TableQuery[WorksToShipsTable]
  private val ficDetails        = TableQuery[FicsDetailsTable]
  private val series            = TableQuery[SeriesTable]
  private val seriesToWorks     = TableQuery[SeriesToWorksTable]
  private val readDates         = TableQuery[ReadDatesTable]

  def getFilteredCards(
      userId: String,
      detailsFilter: Option[FicsDetailsTable => Rep[Boolean]],
      datesFilter: Option[ReadDatesTable => Rep[Boolean]]
  ): IO[DbError, List[FicCard]] = {
    val filteredDetailsIO: IO[DbError, Seq[FicDetailsDoc]] = (detailsFilter, datesFilter) match
      case (Some(detailsF), Some(dateF)) =>
        db.run((for {
          dates   <- readDates if dates.userId === userId && dateF(dates)
          details <- ficDetails if dates.ficId === details.ficId && dates.ficIsSeries === details.ficIsSeries && detailsF(details)
        } yield details).result)
      case (Some(detailsF), _) =>
        db.run(ficDetails.filter(d => d.userId === userId && detailsF(d)).result)
      case (_, Some(dateF)) =>
        db.run((for {
          dates   <- readDates if dates.userId === userId && dateF(dates)
          details <- ficDetails if dates.ficId === details.ficId && dates.ficIsSeries === details.ficIsSeries
        } yield details).result)
      case (None, None) => db.run(ficDetails.filter(d => d.userId === userId).result)
    filteredDetailsIO.flatMap(details => if (details.isEmpty) ZIO.succeed(List()) else collectCardsData(userId, details.distinct))
  }

  private def collectCardsData(userId: String, details: Seq[FicDetailsDoc]): IO[DbError, List[FicCard]] = for {
    seriesIds      <- ZIO.succeed(details.filter(_.ficIsSeries).map(_.ficId))
    seriesDocs     <- db.run(series.filter(_.id.inSet(seriesIds)).result)
    seriesWorksIds <- db.run(seriesToWorks.filter(_.seriesId.inSet(seriesIds)).map(_.workId).result).map(_.distinct)
    workIds        <- ZIO.succeed(details.filter(!_.ficIsSeries).map(_.ficId) ++ seriesWorksIds)
    worksInfos     <- collectWorks(workIds)
    seriesInfos    <- collectSeries(seriesDocs, worksInfos)
    allInfos       <- ZIO.succeed(seriesInfos ++ worksInfos.filter(w => !seriesWorksIds.contains(w.id)))
    detailsByKey   <- ZIO.succeed(details.map(d => ((d.ficId, if (d.ficIsSeries) FicType.Series else FicType.Work), d)).toMap)
  } yield allInfos
    .map(info => FicCard(UserFicKey(userId, info.id, info.ficType), info, detailsByKey((info.id, info.ficType)).toModel))
    .toList

  private def collectWorks(workIds: Seq[String]) = for {
    workDocs <- db.run(works.filter(_.id.inSet(workIds)).result)
    fandomsByWork <- db
      .run(worksToFandoms.filter(_.workId.inSet(workIds)).map(d => (d.workId, d.fandom)).result)
      .map(seq => seq.groupMap((id, _) => id)((_, f) => f))
    charactersByWork <- db
      .run(worksToCharacters.filter(_.workId.inSet(workIds)).map(d => (d.workId, d.character)).result)
      .map(seq => seq.groupMap((id, _) => id)((_, c) => c))
    shipsByWork <- db
      .run(worksToShips.filter(_.workId.inSet(workIds)).map(d => (d.workId, d.shipName)).result)
      .map(seq => seq.groupMap((id, _) => id)((_, s) => s))
    tagsByWork <- db
      .run(worksToTags.filter(_.workId.inSet(workIds)).map(d => (d.workId, d.tagName)).result)
      .map(seq => seq.groupMap((id, _) => id)((_, t) => t))
  } yield workDocs.map(work =>
    Ao3FicInfo(
      id = work.id,
      ficType = FicType.Work,
      link = work.link,
      title = work.title,
      authors = work.authors.split(", ").toList,
      rating = Rating.withName(work.rating),
      categories = work.categories.split(", ").toSet,
      warnings = if (work.warnings.isBlank) Set() else work.warnings.split(", ").toSet,
      fandoms = fandomsByWork.getOrElse(work.id, Seq()).toSet,
      characters = charactersByWork.getOrElse(work.id, Seq()).toSet,
      relationships = shipsByWork.getOrElse(work.id, Seq()).toList.distinct,
      tags = tagsByWork.getOrElse(work.id, Seq()).toList.distinct,
      words = work.words,
      complete = work.complete,
      partsWritten = work.partsWritten,
      downloadLink = work.downloadLink
    )
  )

  private def collectSeries(seriesDocs: Seq[SeriesDoc], allSeriesWorks: Seq[Ao3FicInfo]) = ZIO.collectAll(
    seriesDocs.map(seriesDoc =>
      for {
        workIdsWithPositions <- db.run(
          seriesToWorks.filter(_.seriesId === seriesDoc.id).map(d => (d.workId, d.positionInSeries)).result
        )
        workIds     <- ZIO.succeed(workIdsWithPositions.sortBy(_._2).map(_._1).toSet)
        seriesWorks <- ZIO.succeed(allSeriesWorks.filter(w => workIds.contains(w.id)).toList)
      } yield Ao3FicInfo(
        id = seriesDoc.id,
        ficType = FicType.Series,
        link = seriesDoc.link,
        title = seriesDoc.title,
        authors = seriesDoc.authors.split(", ").toList,
        rating = seriesWorks.map(_.rating).maxBy(_.id),
        categories = seriesWorks.flatMap(_.categories).toSet,
        warnings = seriesWorks.flatMap(_.warnings).toSet,
        fandoms = seriesWorks.flatMap(_.fandoms).toSet,
        characters = seriesWorks.flatMap(_.characters).toSet,
        relationships = seriesWorks.flatMap(_.relationships).distinct,
        tags = seriesWorks.flatMap(_.tags).distinct,
        words = seriesWorks.map(_.words).sum,
        complete = seriesDoc.complete,
        partsWritten = seriesWorks.length,
        downloadLink = None
      )
    )
  )
