package com.kapibarabanka.airtable

case class PatchRequest[A <: EntityDocument](
  records: List[Record[A]]
)
