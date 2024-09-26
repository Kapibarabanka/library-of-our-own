package com.kapibarabanka.kapibarabot.airtable.tables

import com.kapibarabanka.airtable.{TableBase, UpsertParameters}
import com.kapibarabanka.kapibarabot.airtable.docs.FicDisplayDoc
import io.circe.generic.auto.deriveEncoder
import org.http4s.client.Client
import zio.Task

case class FicsTable(token: String, http: Client[Task]) extends TableBase[FicDisplayDoc]("FicsDisplay", token, http):
  override val upsertParameters: UpsertParameters = UpsertParameters(List("Id"))
