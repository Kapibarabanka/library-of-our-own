package com.kapibarabanka.kapibarabot.persistence.tables

import cats.syntax.all.*
import com.kapibarabanka.airtable.*
import com.kapibarabanka.kapibarabot.persistence.docs.{FicDocument, StatsDocument}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.Method
import org.http4s.Method.*
import org.http4s.client.Client
import zio.{IO, Task}

case class StatsTable(token: String, http: Client[Task]) extends TableBase[StatsDocument]("Fics", token, http):
  override val upsertParameters: UpsertParameters = UpsertParameters(List("ao3Id"))

  def patchFicStats(airtableId: String, stats: StatsDocument): IO[AirtableError, Record[FicDocument]] = {
    val json    = PatchRequest(List(Record(Some(airtableId), stats))).asJson.deepDropNullValues.dropEmptyValues
    val request = constructRequest(PATCH, url, json)
    runRequest[RecordsResponse[FicDocument]](request).map(r => r.records.last)
  }
