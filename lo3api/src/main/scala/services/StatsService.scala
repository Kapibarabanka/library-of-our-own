package kapibarabanka.lo3.api
package services

import sqlite.services.Lo3Data
import sqlite.tables.ReadDatesTable

import kapibarabanka.lo3.common.models.api.*
import kapibarabanka.lo3.common.models.domain.{DbError, FicCard}
import slick.jdbc.PostgresProfile.api.*
import zio.IO

import java.time.{LocalDate, Month}
import scala.collection.immutable

object StatsService:
  private val firstOnly = true
  private val topCount  = 5
  def getGeneralStats(userId: String): IO[DbError, List[MonthStats]] = for {
    data <- getHalfYearData(userId)

  } yield data.map((m, fics) => MonthStats(month = m.toString, fics = fics.length, words = fics.map(_.ao3Info.words).sum))

  def getTagFieldStats(userId: String, tagField: StatTagField): IO[DbError, TagFieldStats] =
    val data = getHalfYearData(userId)
    data.map(allData => {
      val tagDataByTime = allData.map((m, cards) => (m, cards.flatMap(c => getTagFieldWordCount(c, tagField, firstOnly))))
      // (tagVal, ficCount, wordCount)
      val totalsByTag = tagDataByTime
        .flatMap((_, t) => t)
        .groupMap((tag, _) => tag)((_, words) => words)
        .toList
        .map((tag, wordsList) => (tag, wordsList.length, wordsList.sum))
      val topByFics    = totalsByTag.sortBy(-_._2).slice(0, topCount).map(_._1)
      val topByWords   = totalsByTag.sortBy(-_._3).slice(0, topCount).map(_._1)
      val allTopLabels = (topByWords ++ topByFics).toSet
      val datasets = tagDataByTime.map((tl, data) => {
        val topData = data.filter((label, _) => allTopLabels.contains(label))
        val byFics = allTopLabels.map(tag =>
          TagDataPoint(tag, if topByFics.contains(tag) then topData.count((label, _) => label == tag) else 0)
        )
        val byWords = allTopLabels.map(tag =>
          TagDataPoint(tag, if topByWords.contains(tag) then topData.filter((label, _) => label == tag).map(_._2).sum else 0)
        )
        TagDataset(timeLabel = tl.toString, byFics = byFics.toList, byWords = byWords.toList)
      })
      TagFieldStats(allTopLabels, labelsByFics = topByFics.toSet, labelsByWords = topByWords.toSet, datasets)
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

  private def getTagFieldWordCount(ficCard: FicCard, field: StatTagField, firstOnly: Boolean) = (field match
    case StatTagField.Ship =>
      if (firstOnly) ficCard.ao3Info.relationships.headOption.map(List(_)).getOrElse(List()) else ficCard.ao3Info.relationships
    case StatTagField.Fandom =>
      if (firstOnly) ficCard.ao3Info.fandoms.headOption.map(List(_)).getOrElse(List()) else ficCard.ao3Info.fandoms.toList
    case StatTagField.Tag => ficCard.ao3Info.tags
  ).map(t => (t, ficCard.ao3Info.words))
