package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.ao3.Series

case class SeriesDoc(
    id: String,
    title: String,
    authors: String,
    link: String,
    started: String,
    updated: Option[String],
    complete: Boolean
)

object SeriesDoc:
  def fromModel(series: Series): SeriesDoc = SeriesDoc(
    id = series.id,
    link = series.link,
    title = series.title,
    authors = series.authors.mkString(", "),
    started = series.started.toString,
    updated = series.updated.map(_.toString),
    complete = series.complete
  )
