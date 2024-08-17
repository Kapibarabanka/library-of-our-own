package com.kapibarabanka.kapibarabot.persistence.tables

import com.kapibarabanka.airtable.{TableBase, UpsertParameters}
import com.kapibarabanka.kapibarabot.persistence.docs.TagDocument
import io.circe.generic.auto.deriveEncoder
import org.http4s.client.Client
import zio.Task

case class CharactersTable(token: String, http: Client[Task]) extends TableBase[TagDocument]("Characters", token, http):
  override val upsertParameters: UpsertParameters = UpsertParameters(List("Name"))
