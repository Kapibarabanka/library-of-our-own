package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

case class Relationship(
    characters: Set[Character] = Set(),
    shipType: RelationshipType.Value,
    nameInFic: Option[String] = None
) extends Tag:
  val name: String          = getShipName
  val category: TagCategory = TagCategory.Relationship
  private def getShipName =
    val separator = shipType match
      case RelationshipType.Platonic => " & "
      case _                         => "/"
    val label            = characters.head.label
    val sortedCharacters = characters.toList.sortBy(c => c.name)
    if (sortedCharacters.forall(_.label == label))
      Ao3TagName.combineWithLabel(sortedCharacters.map(_.name).mkString(separator), label)
    else
      sortedCharacters.map(c => Ao3TagName.combineWithLabel(c.name, c.label)).mkString(separator)

object Relationship:
  implicit val schema: Schema[Relationship] = DeriveSchema.gen
