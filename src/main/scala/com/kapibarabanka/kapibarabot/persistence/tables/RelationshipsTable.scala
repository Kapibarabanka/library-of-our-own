package com.kapibarabanka.kapibarabot.persistence.tables

import com.kapibarabanka.airtable.{TableBase, UpsertParameters}
import com.kapibarabanka.kapibarabot.persistence.docs.RelationshipDocument
import io.circe.generic.auto.deriveEncoder
import org.http4s.client.Client
import zio.Task

case class RelationshipsTable(token: String, http: Client[Task])
    extends TableBase[RelationshipDocument]("Relationships", token, http):
  override val upsertParameters: UpsertParameters = UpsertParameters(List("Name"))
