package com.kapibarabanka.kapibarabot.persistence.tables

import com.kapibarabanka.airtable.{AirtableError, Record, TableBase, UpsertParameters}
import com.kapibarabanka.kapibarabot.persistence.FilterUtils
import com.kapibarabanka.kapibarabot.persistence.docs.FicDisplayDoc
import io.circe.generic.auto.deriveEncoder
import org.http4s.client.Client
import zio.{IO, Task}

case class FicsTable(token: String, http: Client[Task]) extends TableBase[FicDisplayDoc]("FicsDisplay", token, http):
  override val upsertParameters: UpsertParameters = UpsertParameters(List("Id"))
  def findByAo3Id(id: String): IO[AirtableError, Option[Record[FicDisplayDoc]]] = for {
    result <- filter(FilterUtils.filterByAo3Id(id))
  } yield result match
    case ::(head, _) => Some(head)
    case Nil         => None
