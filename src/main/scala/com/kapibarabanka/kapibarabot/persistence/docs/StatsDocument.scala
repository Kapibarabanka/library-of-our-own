package com.kapibarabanka.kapibarabot.persistence.docs

import com.kapibarabanka.airtable.EntityDocument

final case class StatsDocument(
    Read: Option[Boolean] = None,
    Backlog: Option[Boolean] = None,
    IsOnKindle: Option[Boolean] = None,
    ReadDates: Option[String] = None,
    KindleToDo: Option[Boolean] = None,
    Quality: Option[String] = None,
    Comment: Option[String] = None,
    Fire: Option[Boolean] = None
) extends EntityDocument
