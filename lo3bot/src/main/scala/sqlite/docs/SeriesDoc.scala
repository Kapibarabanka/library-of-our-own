package kapibarabanka.lo3.bot
package sqlite.docs

import ao3scrapper.domain.{FicType, Series}

import java.time.LocalDate

case class SeriesDoc(
    id: String,
    title: String,
    authors: String,
    link: String,
    started: String,
    updated: Option[String],
    words: Int,
    complete: Boolean
)

object SeriesDoc:
  def fromModel(series: Series) = SeriesDoc(
    id = series.id,
    link = series.link,
    title = series.title,
    authors = series.authors.mkString(", "),
    started = series.started.toString,
    updated = series.updated.map(_.toString),
    words = series.words,
    complete = series.complete
  )
