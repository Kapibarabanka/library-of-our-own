package com.kapibarabanka.ao3scrapper.internal.docs

import com.kapibarabanka.ao3scrapper.internal.StringUtils.commaStyleToInt
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Element

import java.time.LocalDate
import java.time.format.DateTimeFormatter

protected[ao3scrapper] case class WorkInSeriesDoc(doc: Element, seriesId: String):
  val id: String = (doc >> attr("id")).replace("work_", "")

  private val seriesParts = doc >> element("ul.series") >> elementList("li")
  val indexInSeries: Int =
    (seriesParts.find(el => (el >> element("a") >> attr("href")) == s"/series/$seriesId").get >> text("strong")).toInt

  private val heading       = doc >> element("h4.heading") >> elementList("a")
  val title: String         = heading.find(_.outerHtml.startsWith("<a href=\"/works")).get.text
  val authors: List[String] = heading.filter(_.outerHtml.startsWith("<a rel=\"author\"")).map(_.text)

  private val square           = (doc >> element("ul.required-tags") >> elementList("span")).toArray
  val rating: String           = square(0).text
  val warnings: List[String]   = square(2).text.split(", ").toList
  val categories: List[String] = square(4).text.split(", ").toList
  val complete: Boolean        = square(6).text == "Complete Work"

  val fandoms: Iterable[String]       = doc >> element("h5.fandoms") >> texts("a")
  val relationships: Iterable[String] = doc >> element("ul.tags") >> texts("li.relationships")
  val characters: Iterable[String]    = doc >> element("ul.tags") >> texts("li.characters")
  val freeformTags: Iterable[String]  = doc >> element("ul.tags") >> texts("li.freeforms")

  object stats {
    private val statsElement                          = doc >> element("dl.stats")
    private def getStat(name: String): Option[String] = statsElement >?> text(s"dd.$name")

    val words: Int = getStat("words").map(commaStyleToInt).get
    val (chaptersWritten, chaptersPlanned) = getStat("chapters") match {
      case Some(s"$written/$planned") => (written.toIntOption, planned.toIntOption)
      case _                          => (None, None)
    }
    val comments: Option[Int]  = getStat("comments").map(commaStyleToInt)
    val kudos: Option[Int]     = getStat("kudos").map(commaStyleToInt)
    val bookmarks: Option[Int] = getStat("bookmarks").map(commaStyleToInt)
    val hits: Option[Int]      = getStat("hits").map(commaStyleToInt)

    val date: LocalDate = LocalDate.parse(doc >> text("p.datetime"), DateTimeFormatter.ofPattern("dd MMM uuuu"))
  }
