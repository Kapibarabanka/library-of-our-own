package com.kapibarabanka.kapibarabot.airtable

import com.kapibarabanka.airtable.AirtableError
import com.kapibarabanka.kapibarabot.domain.UserFicRecord
import com.kapibarabanka.kapibarabot.airtable.docs.*
import com.kapibarabanka.kapibarabot.airtable.tables.*
import com.kapibarabanka.kapibarabot.services.CatsHttpClient
import org.http4s.client.Client
import zio.*

trait AirtableClient:
  def upsertFic(fic: UserFicRecord): IO[AirtableError, Unit]

case class AirtableClientImpl(catsClient: CatsHttpClient, authToken: String) extends AirtableClient:
  private val http            = catsClient.http
  private val fics: FicsTable = FicsTable(authToken, http)

  def upsertFic(fic: UserFicRecord): IO[AirtableError, Unit] = for {
    record <- fics.upsert(FicDisplayDoc.fromModel(fic))
  } yield ()

object AirtableClientImpl:
  def layer(authToken: String): ZLayer[CatsHttpClient, Nothing, AirtableClientImpl] = ZLayer {
    for {
      catsClient <- ZIO.service[CatsHttpClient]
    } yield AirtableClientImpl(catsClient, authToken)
  }
