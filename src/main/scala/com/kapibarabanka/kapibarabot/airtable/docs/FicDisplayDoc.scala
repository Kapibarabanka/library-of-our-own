package com.kapibarabanka.kapibarabot.airtable.docs

import com.kapibarabanka.airtable.EntityDocument
import com.kapibarabanka.kapibarabot.domain.UserFicRecord

final case class FicDisplayDoc(
    Title: String,
    Id: String,
    Link: String,
    IsSeries: Boolean = false,
    Authors: String,
    Rating: String,
    Fandoms: String,
    Relationships: String,
    Characters: String,
    Tags: String,
    Words: Int,
    Complete: Boolean = false,

    // my stats
    Read: Boolean = false,
    Backlog: Boolean = false,
    IsOnKindle: Boolean = false,
    ReadDates: Option[String],
    Quality: Option[String],
    Comments: Option[String],
    Fire: Boolean = false
) extends EntityDocument

object FicDisplayDoc:
  def fromModel(record: UserFicRecord) = FicDisplayDoc(
    Title = record.fic.title,
    Id = record.fic.id,
    Link = record.fic.link,
    IsSeries = record.key.ficIsSeries,
    Authors = record.fic.authors.mkString(", "),
    Rating = record.fic.rating.toString.substring(0, 1),
    Fandoms = record.fic.fandoms.mkString(", "),
    Relationships = record.fic.relationships.mkString(", "),
    Characters = record.fic.characters.mkString(", "),
    Tags = record.fic.tags.mkString(", "),
    Words = record.fic.words,
    Complete = record.fic.complete,
    Read = record.details.read,
    Backlog = record.details.backlog,
    IsOnKindle = record.details.isOnKindle,
    ReadDates = Some(record.readDatesInfo.readDates.mkString("; ")),
    Quality = record.details.quality.map(_.toString),
    Comments = Some(record.comments.mkString("\n")),
    Fire = record.details.fire
  )
