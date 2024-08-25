package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.kapibarabot.domain.MyFicModel
import slick.jdbc.PostgresProfile.api.*

case class FicDoc(id: String, title: String)

object FicDoc:
  def fromModel(fic: MyFicModel): FicDoc = FicDoc(id = fic.id, title = fic.title)

class FicsTable(tag: Tag) extends Table[FicDoc](tag, "Fics"):
  def id    = column[String]("id", O.PrimaryKey, O.Unique)
  def title = column[String]("title")

  def * = (id, title).mapTo[FicDoc]
