package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.*

import java.time.LocalDate

case class WorkDoc(
    id: String,
    title: String,
    authors: String,
    rating: String,
    warnings: String,
    categories: String,
    link: String,
    started: String,
    updated: Option[String],
    words: Int,
    complete: Boolean,
    partsWritten: Int,

    // stats
    read: Boolean,
    backlog: Boolean,
    isOnKindle: Boolean,
    readDates: Option[String],
    quality: Option[String],
    fire: Boolean,
    docCreated: String
)

object WorkDoc:
  def fromModel(work: Work): WorkDoc = WorkDoc(
    id = work.id,
    title = work.title,
    authors = {
      work.authors match
        case Nil    => "Anonymous"
        case values => values.mkString(", ")
    },
    rating = work.rating.toString,
    warnings = work.warnings.map(_.name).mkString(", "),
    categories = work.categories.map(_.toString).mkString(", "),
    started = work.date match {
      case Published(date)                   => date.toString
      case PublishedAndUpdated(published, _) => published.toString
      case SingleDate(date)                  => date.toString
    },
    updated = work.date match {
      case Published(date)                         => None
      case PublishedAndUpdated(published, updated) => Some(updated.toString)
      case SingleDate(date)                        => Some(date.toString)
    },
    words = work.words,
    link = work.link,
    complete = work.complete,
    partsWritten = work.chaptersWritten,
    read = false,
    backlog = false,
    isOnKindle = false,
    readDates = None,
    quality = None,
    fire = false,
    docCreated = LocalDate.now().toString
  )
