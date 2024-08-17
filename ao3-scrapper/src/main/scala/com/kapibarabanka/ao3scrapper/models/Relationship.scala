package com.kapibarabanka.ao3scrapper.models

import com.kapibarabanka.ao3scrapper.models.RelationshipType.*

case class Relationship(
  characters: Set[Character],
  shipType: RelationshipType.Value,
  nameInFic: Option[String] = None
) extends Tag:
  val name: String = getShipName
  val category: TagCategory = TagCategory.Relationship
  private def getShipName = characters.map(_.name).mkString(shipType match
    case Platonic => " & "
    case _ => "/")
