package kapibarabanka.lo3.api
package services

import sqlite.services.Lo3Data
import sqlite.tables.ReadDatesTable

import kapibarabanka.lo3.common.models.api.{MonthStats, StatTagField, TagDataset, TagFieldStats}
import kapibarabanka.lo3.common.models.domain.{DbError, FicCard}
import slick.jdbc.PostgresProfile.api.*
import zio.IO

import java.time.{LocalDate, Month}
import scala.collection.immutable

object StatsService:
  def getGeneralStats(userId: String): IO[DbError, List[MonthStats]] = for {
    data <- getHalfYearData(userId)
  } yield data.map((m, fics) => MonthStats(month = m.toString, fics = fics.length, words = fics.map(_.ao3Info.words).sum))

  def getTagStats(userId: String, tagField: StatTagField): IO[DbError, TagFieldStats] =
    val data = getHalfYearData(userId)
    data.map(allData => {
      val tagData = allData.map((m, cards) => (m, cards.flatMap(c => getTagFieldValues(c, tagField, true))))
      val byTag = tagData
        .flatMap((_, t) => t)
        .groupMap((tag, _) => tag)((_, words) => words)
        .toList
        .map((tag, wordsList) => (tag, wordsList.length, wordsList.sum))
      val topCount   = 5
      val topByFics  = byTag.sortBy(-_._2).slice(0, topCount).map(_._1)
      val topByWords = byTag.sortBy(-_._3).slice(0, topCount).map(_._1)
      val datasetsByFics =
        topByFics.map(tag => TagDataset(tagValue = tag, counts = tagData.map((_, data) => data.count((t, _) => t == tag))))
      val datasetsByWords =
        topByWords.map(tag =>
          TagDataset(tagValue = tag, counts = tagData.map((_, data) => data.filter((t, _) => t == tag).map(_._2).sum / 1000))
        )
      TagFieldStats(months = tagData.map(_._1.toString), byFics = datasetsByFics, byWords = datasetsByWords)
    })

  private def getHalfYearData(userId: String): IO[DbError, List[(Month, List[FicCard])]] =
    val today       = LocalDate.now()
    val startDate   = today.minusMonths(5)
    val datesFilter = (d: ReadDatesTable) => d.endDate.asColumnOf[String] >= startDate.toString && !d.isAbandoned
    val datesAndFics = for {
      datesDocs <- Lo3Data.readDates.getReadDocs(
        userId,
        datesFilter
      )
      fics <- Lo3Data.fics.getFilteredCards(
        userId,
        None,
        Some(datesFilter)
      )
    } yield (datesDocs, fics)
    datesAndFics.map((datesDocs, fics) => {
      val ficsByKey      = fics.map(f => ((f.key.ficId, f.key.ficIsSeries), f)).toMap
      val datesWithFics  = datesDocs.map(d => (LocalDate.parse(d.endDate.get), ficsByKey(d.ficId, d.ficIsSeries)))
      val monthsWithFics = datesWithFics.map((d, f) => ((d.getMonth, d.getYear), f))
      val withYears      = monthsWithFics.groupMap((m, _) => m)((_, f) => f).toList.map((d, f) => (d._1, d._2, f))
      withYears.sortBy((m, y, _) => (y, m)).map((m, _, f) => (m, f))
    })

  private def getTagFieldValues(ficCard: FicCard, field: StatTagField, firstOnly: Boolean) = (field match
    case StatTagField.Ship =>
      if (firstOnly) ficCard.ao3Info.relationships.headOption.map(List(_)).getOrElse(List()) else ficCard.ao3Info.relationships
    case StatTagField.Fandom =>
      if (firstOnly) ficCard.ao3Info.fandoms.headOption.map(List(_)).getOrElse(List()) else ficCard.ao3Info.fandoms.toList
    case StatTagField.Tag => ficCard.ao3Info.tags
  ).map(t => (t, ficCard.ao3Info.words))
