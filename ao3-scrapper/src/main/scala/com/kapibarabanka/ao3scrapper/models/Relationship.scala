package com.kapibarabanka.ao3scrapper.models

import com.kapibarabanka.ao3scrapper.StringUtils
import com.kapibarabanka.ao3scrapper.models.RelationshipType.*

case class Relationship(
    characters: Set[Character],
    shipType: RelationshipType.Value,
    nameInFic: Option[String] = None
) extends Tag:
  val name: String          = getShipName
  val category: TagCategory = TagCategory.Relationship
  private def getShipName =
    val separator = shipType match
      case Platonic => " & "
      case _        => "/"
    val label = characters.head.label
    if (characters.forall(_.label == label))
      StringUtils.combineWithLabel(characters.map(_.name).mkString(separator), label)
    else
      characters.map(c => StringUtils.combineWithLabel(c.name, c.label)).mkString(separator)
