package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.StringUtils
import slick.jdbc.PostgresProfile.api.*
import com.kapibarabanka.ao3scrapper.models.Fandom

case class FandomDoc(fullName: String, name: String, label: Option[String])

object FandomDoc:
  def fromModel(model: Fandom): FandomDoc =
    FandomDoc(StringUtils.combineWithLabel(model.name, model.label), model.name, model.label)

class FandomsTable(tag: Tag) extends Table[FandomDoc](tag, "Fandoms"):
  def fullName = column[String]("fullName", O.PrimaryKey)
  def name     = column[String]("name")
  def label    = column[Option[String]]("label")

  def * = (fullName, name, label).mapTo[FandomDoc]

case class FicsToFandomsDoc(id: Option[Int], ficId: String, Fandom: String)

class FicsToFandomsTable(tag: Tag) extends Table[FicsToFandomsDoc](tag, "FicsToFandoms"):
  def id     = column[Int]("id", O.PrimaryKey, O.Unique)
  def ficId  = column[String]("ficId")
  def fandom = column[String]("fandom")

  def * = (id.?, ficId, fandom).mapTo[FicsToFandomsDoc]
