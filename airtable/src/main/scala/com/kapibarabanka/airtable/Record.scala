package com.kapibarabanka.airtable

import com.kapibarabanka.airtable.EntityDocument

case class Record[A <: EntityDocument](id: Option[String], fields: A)

