package com.kapibarabanka.airtable

import zio.{IO, Task}

trait Table[A <: EntityDocument] {
  val upsertParameters: UpsertParameters

  def upsert(records: List[A]): IO[AirtableError, List[Record[A]]]

  def upsert(doc: A): IO[AirtableError, Record[A]]

  def patch(record: Record[A]): IO[AirtableError, Record[A]]

  def patch(record: List[Record[A]]): IO[AirtableError, List[Record[A]]]

  def findOption(id: String): IO[AirtableError, Option[Record[A]]]

  def find(id: String): IO[AirtableError, Record[A]]

  def delete(id: String): IO[AirtableError, Unit]

  def delete(ids: List[String]): IO[AirtableError, Unit]

  def filter(filter: FilteredRequest): IO[AirtableError, List[Record[A]]]

  def getAll: IO[AirtableError, List[Record[A]]]
}
