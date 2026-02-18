package kapibarabanka.lo3.api
package services

import sqlite.services.Lo3Data
import sqlite.tables.ReadDatesTable

import kapibarabanka.lo3.common.models.api.*
import kapibarabanka.lo3.common.models.domain.{Ao3FicInfo, DbError, Fic}
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
      val tagDataByTime = allData.map((m, cards) => (m, cards.flatMap(f => getTagFieldWordCount(f.ao3Info, tagField, firstOnly))))
      // (tagVal, ficCount, wordCount)
      val totalsByTag = tagDataByTime
        .flatMap((_, t) => t)
        .groupMap((tag, _) => tag)((_, words) => words)
        .toList
        .map((tag, wordsList) => (tag, wordsList.length, wordsList.sum))
      val topByFics    = totalsByTag.sortBy(-_._2).slice(0, topCount).map((label, count, _) => (label, count))
      val topByWords   = totalsByTag.sortBy(-_._3).slice(0, topCount).map((label, _, count) => (label, count))
      val allTopLabels = (topByWords ++ topByFics).map(_._1).toSet
      val datasets = tagDataByTime.map((tl, data) => {
        val topData = data.filter((label, _) => allTopLabels.contains(label))
        val byFics = allTopLabels.map(tag =>
          TagDataPoint(tag, if topByFics.exists(_._1 == tag) then topData.count((label, _) => label == tag) else 0)
        )
        val byWords = allTopLabels.map(tag =>
          TagDataPoint(
            tag,
            if topByWords.exists(_._1 == tag) then topData.filter((label, _) => label == tag).map(_._2).sum else 0
          )
        )
        TagDataset(timeLabel = tl.toString, byFics = byFics.toList, byWords = byWords.toList)
      })
      TagFieldStats(
        allTopLabels,
        topByFics = topByFics.map((l, c) => TagDataPoint(l, c)),
        topByWords = topByWords.map((l, c) => TagDataPoint(l, c)),
        datasets
      )
    })

  private def getHalfYearData(userId: String): IO[DbError, List[(Month, List[Fic])]] =
    val today       = LocalDate.now()
    val startDate   = today.minusMonths(5)
    val datesFilter = (d: ReadDatesTable) => d.endDate.asColumnOf[String] >= startDate.toString && !d.isAbandoned
    val ficsIO      = Lo3Data.fics.getFilteredFics(userId, None, Some(datesFilter))
    ficsIO.map(fics => {
      val datesWithFics  = fics.flatMap(fic => fic.readDatesInfo.readDates.map(d => (d.finishDate.get, fic)))
      val monthsWithFics = datesWithFics.map((d, f) => ((d.getMonth, d.getYear), f))
      val withYears      = monthsWithFics.groupMap((m, _) => m)((_, f) => f).toList.map((d, f) => (d._1, d._2, f))
      withYears.sortBy((m, y, _) => (y, m)).map((m, _, f) => (m, f))
    })

  private def getTagFieldWordCount(info: Ao3FicInfo, field: StatTagField, firstOnly: Boolean) = (field match
    case StatTagField.Ship =>
      if (firstOnly) info.relationships.headOption.map(List(_)).getOrElse(List()) else info.relationships
    case StatTagField.Fandom =>
      if (firstOnly) info.fandoms.headOption.map(List(_)).getOrElse(List()) else info.fandoms.toList
    case StatTagField.Freeform => info.tags
  ).map(t => (t, info.words))
