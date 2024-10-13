package kapibarabanka.lo3.bot
package sqlite.docs

import ao3scrapper.domain.{Character, Relationship, RelationshipType}

case class RelationshipDoc(name: String, relationshipType: String, nameInFic: Option[String]):
  def toModel(characters: Set[Character]): Relationship =
    Relationship(characters, RelationshipType.withName(relationshipType), nameInFic)

object RelationshipDoc:
  def fromModel(model: Relationship): RelationshipDoc =
    RelationshipDoc(model.nameInFic.getOrElse(model.name), model.shipType.toString, model.nameInFic)
