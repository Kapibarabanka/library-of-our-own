package kapibarabanka.lo3.api
package ficService.internal

import ficService.internal.StringUtils.{commaStyleToInt, parseDate}

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document

protected[ficService] case class WorkHtml(doc: Document):
  val title   = doc >> text("h2.title")
  val authors = doc >> element("h3.byline") >> texts("a")
  val rating  = doc >> text("dd.rating")

  // TODO: add more formats
  val mobiLink =
    (doc >> element("li.download") >> element("ul") >> elements("a")).map(l => l >> attr("href")).find(s => s.contains(".mobi"))

  private def getTagGroup(name: String) = (doc >?> element(s"dd.$name") >> texts(".tag")).map(_.toList) getOrElse List()

  lazy val warnings      = getTagGroup("warning")
  lazy val categories    = getTagGroup("category")
  lazy val fandoms       = getTagGroup("fandom")
  lazy val relationships = getTagGroup("relationship")
  lazy val characters    = getTagGroup("character")
  lazy val freeformTags  = getTagGroup("freeform")

  object stats {
    private val statsElement                          = doc >> element("dl.stats")
    private def getStat(name: String): Option[String] = statsElement >?> text(s"dd.$name")

    val published = getStat("published").map(parseDate).get
    val updated   = getStat("status").map(parseDate)
    val words     = getStat("words").map(commaStyleToInt).get
    val (chaptersWritten, chaptersPlanned) = getStat("chapters") match {
      case Some(s"$written/$planned") => (written.toIntOption, planned.toIntOption)
      case _                          => (None, None)
    }
    val comments  = getStat("comments").map(commaStyleToInt)
    val kudos     = getStat("kudos").map(commaStyleToInt)
    val bookmarks = getStat("bookmarks").map(commaStyleToInt)
    val hits      = getStat("hits").map(commaStyleToInt)
  }
