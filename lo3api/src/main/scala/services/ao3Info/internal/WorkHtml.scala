package kapibarabanka.lo3.api
package services.ao3Info.internal

import StringUtils.{commaStyleToInt, parseDate}

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document

protected[ao3Info] case class WorkHtml(doc: Document) extends WorkDoc:
  val title: String = doc >> text("h2.title")
  val authors       = doc >> element("h3.byline") >> texts("a")
  val rating        = doc >> text("dd.rating")

  val mobiLink =
    (doc >> element("li.download") >> element("ul") >> elements("a")).map(l => l >> attr("href")).find(s => s.contains(".mobi"))

  private def getTagGroup(name: String) = (doc >?> element(s"dd.$name") >> texts(".tag")).map(_.toList) getOrElse List()

  val warnings      = getTagGroup("warning")
  val categories    = getTagGroup("category")
  val fandoms       = getTagGroup("fandom")
  val relationships = getTagGroup("relationship")
  val characters    = getTagGroup("character")
  val freeformTags  = getTagGroup("freeform")

  private val statsElement                          = doc >> element("dl.stats")
  private def getStat(name: String): Option[String] = statsElement >?> text(s"dd.$name")

  val published = getStat("published").map(parseDate).get
  val updated   = getStat("status").map(parseDate)
  val words     = getStat("words").map(commaStyleToInt).get
  val (chaptersWritten, chaptersPlanned) = getStat("chapters") match {
    case Some(s"$written/$planned") => (written.toIntOption, planned.toIntOption)
    case _                          => (None, None)
  }
