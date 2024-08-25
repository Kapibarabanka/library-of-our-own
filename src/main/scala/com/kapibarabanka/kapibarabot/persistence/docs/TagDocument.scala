package com.kapibarabanka.kapibarabot.persistence.docs

import com.kapibarabanka.airtable.EntityDocument

case class TagDocument(Name: String, Label: Option[String], WorkCount: Option[Int] = None) extends EntityDocument, WithWorkCount
