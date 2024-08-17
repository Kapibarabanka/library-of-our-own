package com.kapibarabanka.kapibarabot.persistence.docs

import com.kapibarabanka.airtable.EntityDocument

final case class FicDocument(
    Name: String,
    Author: String,
    Rating: String,
    Warnings: String,
    Categories: List[String] = List(),
    Fandoms: List[String] = List(),
    Relationships: List[String] = List(),
    Characters: List[String] = List(),
    AllCharacters: Option[String],
    Tags: List[String] = List(),
    AllTags: Option[String],
    FilterableTags: Option[String],
    Published: String,
    Updated: Option[String],
    Words: Long,
    ChaptersWritten: Int,
    Completed: Boolean = false,
    Link: String,
    ao3Id: String,
    // TODO: fix the json decoding, false from the Airtable is not set:
    //  Returned records do not include any fields with "empty" values, e.g. "", [], or false.
    IsSeries: Boolean = false,

    // my stats
    Read: Option[Boolean] = None,
    Backlog: Option[Boolean] = None,
    IsOnKindle: Option[Boolean] = None,
    ReadDates: Option[String] = None,
    KindleToDo: Option[Boolean] = None,
    Quality: Option[String] = None,
    Comment: Option[String] = None
) extends EntityDocument
