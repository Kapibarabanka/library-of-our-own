package kapibarabanka.lo3.bot
package ao3scrapper.internal

import ao3scrapper.internal.StringUtils.{commaStyleToInt, parseDate}
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.model.Document

import scala.language.postfixOps
protected[ao3scrapper] case class SeriesPageDoc(doc: Document, seriesId: String, page: Int):
  val title                   = doc >> text("h2.heading")
  private val metaDataElement = doc >> element("dl.series")
  private val labels          = (metaDataElement >> texts("dt")).map(_.replace(":", ""))
  private val data            = metaDataElement >> texts("dd")
  private val metadata        = labels.zip(data).toMap
  val authors                 = if (metadata.contains("Creator")) metadata.get("Creator") else metadata.get("Creators")
  val begun                   = metadata.get("Series Begun").map(parseDate)
  val updated                 = metadata.get("Series Updated").map(parseDate)
  val description             = metadata.get("Description")
  val words                   = metadata.get("Words").map(commaStyleToInt)
  val complete                = metadata.get("Complete").map(_ == "Yes")
  val bookmarks               = metadata.get("Bookmarks").map(commaStyleToInt)
  val pageCount               = (doc >?> element(".pagination") >> texts("li")).map(_.flatMap(_.toIntOption).max)
  val workElements            = doc >> elementList("li.work")
  val works                   = workElements.map(WorkInSeriesDoc(_, seriesId))
  val squareTags              = workElements >> element("ul.required-tags") >> texts("li")

  val (ratings, allWarnings, allCategories) = squareTags.flatMap {
    case List(r: String, w: String, c: String, _) => Some(r, w, c)
    case _                                        => None
  } unzip3

  val fandoms       = (workElements >> element("h5.fandoms") >> texts("a")).flatten
  val relationships = (workElements >> element("ul.tags") >> texts("li.relationships")).flatten.distinct
  val characters    = (workElements >> element("ul.tags") >> texts("li.characters")).flatten.toSet
  val freeformTags  = (workElements >> element("ul.tags") >> texts("li.freeforms")).flatten.distinct

  val workIds = doc >> elementList("li.work") >> attr("id") map (_.replace("work_", ""))
