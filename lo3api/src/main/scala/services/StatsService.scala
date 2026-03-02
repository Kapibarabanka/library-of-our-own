package kapibarabanka.lo3.api
package services

import sqlite.services.Lo3Data
import sqlite.tables.ReadDatesTable

import kapibarabanka.lo3.common.models.api.*
import kapibarabanka.lo3.common.models.domain.{Ao3FicInfo, DbError, Fic}
import slick.jdbc.PostgresProfile.api.*
import zio.IO

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, Month}
import scala.collection.immutable
import scala.math.Ordered.orderingToOrdered

case class DataWithRange[TData](data: TData, start: LocalDate, end: LocalDate, key: String)
case class MonthYear(year: Int, month: Month)
case class FicReadRate(fic: Fic, rate: Double, readKey: String)

object StatsService:
  private val firstOnly = true
  private val topCount  = 5
  def getGeneralStats(userId: String): IO[DbError, CountStats] = for {
    data <- getHalfYearData(userId)

  } yield CountStats(
    stats = data.map((m, rates) =>
      MonthCountStats(
        month = m.month.toString,
        fics = rates.length,
        words = rates.map(readRate => (readRate.fic.ao3Info.words * readRate.rate).round).sum
      )
    ),
    totalFics = data.flatMap((_, readRates) => readRates).map(_.readKey).distinct.length
  )

  def getTagFieldStats(userId: String, tagField: StatTagField): IO[DbError, TagFieldStats] =
    val data = getHalfYearData(userId)
    data.map(allData => {
      val tagDataByTime =
        allData.map((m, readRates) =>
          (m, readRates.flatMap(readRate => getTagFieldWordCount(readRate.fic.ao3Info, tagField, firstOnly, readRate.rate)))
        )
      // (tagVal, ficCount, wordCount)
      val totalsByTag = tagDataByTime
        .flatMap((_, t) => t)
        .groupMap((tag, _) => tag)((_, words) => words)
        .toList
        .map((tag, wordsList) => (tag, wordsList.length, wordsList.sum))
      val topByFics    = totalsByTag.sortBy(-_._2).slice(0, topCount).map((label, count, _) => (label, count))
      val topByWords   = totalsByTag.sortBy(-_._3).slice(0, topCount).map((label, _, count) => (label, count))
      val allTopLabels = (topByWords ++ topByFics).map(_._1).toSet
      val datasets = tagDataByTime.map((monthYear, data) => {
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
        TagDataset(timeLabel = monthYear.month.toString, byFics = byFics.toList, byWords = byWords.toList)
      })
      TagFieldStats(
        allTopLabels,
        topByFics = topByFics.map((l, c) => TagDataPoint(l, c)),
        topByWords = topByWords.map((l, c) => TagDataPoint(l, c)),
        datasets
      )
    })

  private def getHalfYearData(userId: String): IO[DbError, List[(MonthYear, List[FicReadRate])]] =
    val monthCount  = 6
    val today       = LocalDate.now()
    val startDate   = today.minusMonths(monthCount - 1)
    val datesFilter = (d: ReadDatesTable) => d.endDate.asColumnOf[String] >= startDate.toString && !d.isAbandoned
    val ficsIO      = Lo3Data.fics.getFilteredFics(userId, None, Some(datesFilter))

    val currentMonthStart = LocalDate.of(today.getYear, today.getMonth, 1)
    val monthData = (0 to 5).toList.reverse
      .map(currentMonthStart.minusMonths(_))
      .map(start => {
        val my = MonthYear(start.getYear, start.getMonth)
        DataWithRange(my, start, start.plusDays(start.lengthOfMonth - 1), my.toString)
      })

    def getFicMonthRates(ficRange: DataWithRange[Fic]): List[(MonthYear, FicReadRate)] = {
      val ficEndExclusive = ficRange.end.plusDays(1)
      val ficDays         = ChronoUnit.DAYS.between(ficRange.start, ficEndExclusive)
      monthData
        .map(monthRange => {
          val dayCount =
            if ficRange.start > monthRange.end || ficRange.end < monthRange.start then 0
            else {
              val ficMonthStart = if ficRange.start > monthRange.start then ficRange.start else monthRange.start
              val ficMonthEnd   = if ficRange.end < monthRange.end then ficRange.end else monthRange.end
              ChronoUnit.DAYS.between(ficMonthStart, ficMonthEnd.plusDays(1))
            }
          (monthRange.data, ficRange.data, dayCount)
        })
        .filter((_, _, dayCount) => dayCount != 0)
        .map((m, f, d) => (m, FicReadRate(f, d.toDouble / ficDays, ficRange.key)))
    }

    ficsIO.map(fics => {
      val ficsRanges =
        fics.flatMap(fic =>
          fic.readDatesInfo.readDates.map(d =>
            DataWithRange(
              fic,
              LocalDate.from(d.startDate),
              LocalDate.from(d.finishDate.get),
              d.id.map(_.toString).getOrElse("noKey")
            )
          )
        )
      val ficsWithRates = ficsRanges.map(getFicMonthRates)
      ficsWithRates.flatten
        .groupMap((my, _) => my)((_, rate) => rate)
        .toList
        .sortBy((my, _) => (my.year, my.month))
    })

  private def getTagFieldWordCount(info: Ao3FicInfo, field: StatTagField, firstOnly: Boolean, rate: Double) = (field match
    case StatTagField.Ship =>
      if (firstOnly) info.relationships.headOption.map(List(_)).getOrElse(List()) else info.relationships
    case StatTagField.Fandom =>
      if (firstOnly) info.fandoms.headOption.map(List(_)).getOrElse(List()) else info.fandoms.toList
    case StatTagField.Freeform => info.freeformTags.map(_.canonicalName)
  ).map(t => (t, (info.words * rate).round))
