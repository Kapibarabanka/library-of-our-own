package com.kapibarabanka.kapibarabot.persistence.docs

import com.kapibarabanka.airtable.EntityDocument

case class RelationshipDocument(
  Name: String,
  Characters: List[String],
  Type: String,
  WorkCount: Option[Int] = None
) extends EntityDocument, WithWorkCount
