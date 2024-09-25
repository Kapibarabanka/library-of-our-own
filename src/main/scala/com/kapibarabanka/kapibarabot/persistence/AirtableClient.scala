package com.kapibarabanka.kapibarabot.persistence

import com.kapibarabanka.kapibarabot.domain.UserFicRecord
import com.kapibarabanka.kapibarabot.persistence.docs.*
import com.kapibarabanka.kapibarabot.persistence.tables.*
import org.http4s.client.Client
import zio.*

class AirtableClient(http: Client[Task], authToken: String):
  val fics: FicsTable = FicsTable(authToken, http)

  def upsertFic(fic: UserFicRecord) = for {
    record <- fics.upsert(FicDisplayDoc.fromModel(fic))
  } yield ()
