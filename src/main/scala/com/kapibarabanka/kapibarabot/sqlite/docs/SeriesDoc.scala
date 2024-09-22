package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.{FicType, Series}
import com.kapibarabanka.kapibarabot.domain.FicKey

import java.time.LocalDate

case class SeriesDoc(
    id: String,
    title: String,
    authors: String,
    link: String,
    started: String,
    updated: Option[String],
    words: Int,
    complete: Boolean,

    // stats
//    read: Boolean, if all works are read the series is read
    backlog: Boolean,
    isOnKindle: Boolean,
//    readDates: Option[String], if date is similar across all works take it as series read date
    docCreated: String
):
  val key: FicKey = FicKey(id, FicType.Series)

object SeriesDoc:
  def fromModel(series: Series) = SeriesDoc(
    id = series.id,
    link = series.link,
    title = series.title,
    authors = series.authors.mkString(", "),
    started = series.started.toString,
    updated = series.updated.map(_.toString),
    words = series.words,
    complete = series.complete,
    backlog = false,
    isOnKindle = false,
    docCreated = LocalDate.now().toString
  )
end SeriesDoc
