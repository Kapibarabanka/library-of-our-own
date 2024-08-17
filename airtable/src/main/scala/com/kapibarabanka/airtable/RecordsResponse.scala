package com.kapibarabanka.airtable

case class RecordsResponse[A <: EntityDocument](records: List[Record[A]], offset: Option[String])
