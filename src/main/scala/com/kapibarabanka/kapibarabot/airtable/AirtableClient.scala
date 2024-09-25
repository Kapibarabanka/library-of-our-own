package com.kapibarabanka.kapibarabot.airtable

import com.kapibarabanka.kapibarabot.domain.UserFicRecord
import com.kapibarabanka.kapibarabot.airtable.docs.*
import com.kapibarabanka.kapibarabot.airtable.tables.*
import org.http4s.client.Client
import zio.*

class AirtableClient(http: Client[Task], authToken: String):
  val fics: FicsTable = FicsTable(authToken, http)

  def upsertFic(fic: UserFicRecord) = for {
    record <- fics.upsert(FicDisplayDoc.fromModel(fic))
  } yield ()
