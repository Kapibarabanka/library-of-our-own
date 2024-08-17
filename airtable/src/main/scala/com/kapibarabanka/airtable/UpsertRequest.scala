package com.kapibarabanka.airtable

case class UpsertRequest[A <: EntityDocument](
  records: List[Record[A]],
  performUpsert: UpsertParameters
)
