package com.kapibarabanka.airtable

case class Record[A <: EntityDocument](id: Option[String], fields: A)

