package com.kapibarabanka.kapibarabot.persistence.docs

import com.kapibarabanka.airtable.EntityDocument
import com.kapibarabanka.ao3scrapper.models.FicType
import com.kapibarabanka.kapibarabot.domain.FicDisplayModel

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
  def fromModel(fic: FicDisplayModel) = FicDisplayDoc(
    Title = fic.title,
    Id = fic.id,
    Link = fic.link,
    IsSeries = fic.ficType == FicType.Series,
    Authors = fic.authors.mkString(", "),
    Rating = fic.rating.toString.substring(0, 1),
    Fandoms = fic.fandoms.mkString(", "),
    Relationships = fic.relationships.mkString(", "),
    Characters = fic.characters.mkString(", "),
    Tags = fic.tags.mkString(", "),
    Words = fic.words,
    Complete = fic.complete,
    Read = fic.stats.read,
    Backlog = fic.stats.backlog,
    IsOnKindle = fic.stats.isOnKindle,
    ReadDates = Some(fic.readDates.mkString(",")),
    Quality = fic.stats.quality.map(_.toString),
    Comments = Some(fic.comments.mkString("\n")),
    Fire = fic.stats.fire
  )
