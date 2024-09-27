package com.kapibarabanka.ao3scrapper.domain

import com.kapibarabanka.ao3scrapper.domain.RelationshipType.*
import com.kapibarabanka.ao3scrapper.utils.StringUtils

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
    val label            = characters.head.label
    val sortedCharacters = characters.toList.sortBy(c => c.name)
    if (sortedCharacters.forall(_.label == label))
      StringUtils.combineWithLabel(sortedCharacters.map(_.name).mkString(separator), label)
    else
      sortedCharacters.map(c => StringUtils.combineWithLabel(c.name, c.label)).mkString(separator)
