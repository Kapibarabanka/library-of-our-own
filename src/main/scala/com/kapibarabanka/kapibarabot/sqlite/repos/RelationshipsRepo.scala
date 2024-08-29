package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.ao3scrapper.models.Relationship
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.RelationshipDoc
import com.kapibarabanka.kapibarabot.sqlite.tables.{FicsToShipsTable, RelationshipsTable}
import slick.jdbc.PostgresProfile.api.*
import zio.ZIO

class RelationshipsRepo(userId: String) extends WithDb(userId):
  private val ships          = TableQuery[RelationshipsTable]
  private val ficsToShips    = TableQuery[FicsToShipsTable]
  private val charactersRepo = CharactersRepo(userId)

  def add(ship: Relationship): ZIO[Any, Throwable, Unit] = for {
    _ <- db(ships += RelationshipDoc.fromModel(ship))
    _ <- charactersRepo.addAndLinkToShip(ship.characters.toList, ship.name)
  } yield ()

  def getFicShips(ficId: String) = db((for {
    doc        <- ficsToShips if doc.ficId === ficId
    shipDoc    <- ships if shipDoc.name === doc.shipName
  } yield shipDoc).result)
