package kapibarabanka.lo3.models
package ao3

import ao3.RelationshipType.Platonic

import zio.schema.{DeriveSchema, Schema}

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
      Ao3TagName.combineWithLabel(sortedCharacters.map(_.name).mkString(separator), label)
    else
      sortedCharacters.map(c => Ao3TagName.combineWithLabel(c.name, c.label)).mkString(separator)

object Relationship:
  implicit val schema: Schema[Relationship] = DeriveSchema.gen
