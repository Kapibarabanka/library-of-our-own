package kapibarabanka.lo3.api
package services

import sqlite.services.Lo3Data
import sqlite.tables.ReadDatesTable

import kapibarabanka.lo3.common.models.api.MonthStats
import kapibarabanka.lo3.common.models.domain.{DbError, FicCard}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

import java.time.{LocalDate, Month}
import scala.collection.immutable

object StatsService:
  def getGeneralStats(userId: String): IO[DbError, List[MonthStats]] = for {
    data <- getHalfYearData(userId)
  } yield data.map((m, fics) => MonthStats(month = m.toString, fics = fics.length, words = fics.map(_.ao3Info.words).sum / 1000))

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
