package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.Series

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
    complete = series.complete,
    backlog = false,
    isOnKindle = false,
    docCreated = LocalDate.now().toString
  )
end SeriesDoc
