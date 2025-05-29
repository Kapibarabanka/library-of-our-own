package kapibarabanka.lo3.api
package services.ao3Info.internal

import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document
import StringUtils.{commaStyleToInt, parseDate}

import java.time.LocalDate
class RestrictedWorkHtml(doc: Document, htmlLink: String) extends WorkDoc {

  override val title: String             = doc >> text("h1")
  override val authors: Iterable[String] = doc >> element("div.byline") >> texts("a")
  override val mobiLink: Option[String]  = Some(htmlLink.replace("html", "mobi"))

  private val tagsEl  = doc >> element("dl.tags")
  private val labels  = tagsEl >> texts("dt")
  private val tags    = (tagsEl >> elements("dd")).map(e => e >> texts("a"))
  private val tagsMap = labels.zip(tags)

  private def getTags(labelStart: String) = tagsMap.find(_._1.startsWith(labelStart)).map(_._2.toList)

  val rating        = getTags("Rating").get.head
  val warnings      = getTags("Archive Warning").getOrElse(List())
  val categories    = getTags("Categor").getOrElse(List())
  val fandoms       = getTags("Fandom").getOrElse(List())
  val relationships = getTags("Relationship").getOrElse(List())
  val characters    = getTags("Character").getOrElse(List())
  val freeformTags  = getTags("Additional Tag").getOrElse(List())

  private val tagsTexts  = tagsEl >> texts("dd")
  private val statsBlock = labels.zip(tagsTexts).find(_._1 == "Stats:").get._2
  private val statsTexts = statsBlock.split(" ").toList.grouped(2).toList

  private def getStat(label: String) = statsTexts.find(_.head == label).map(_(1))

  val published                  = getStat("Published:").map(parseDate).get
  val updated: Option[LocalDate] = getStat("Updated:").orElse(getStat("Completed:")).map(parseDate)
  val words: Int                 = getStat("Words:").map(commaStyleToInt).get
  val (chaptersWritten, chaptersPlanned) = getStat("Chapters:") match {
    case Some(s"$written/$planned") => (written.toIntOption, planned.toIntOption)
    case _                          => (None, None)
  }
}
