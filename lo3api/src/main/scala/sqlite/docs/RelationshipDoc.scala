package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.models.ao3.{Character, Relationship, RelationshipType}

case class RelationshipDoc(name: String, relationshipType: String, nameInFic: Option[String]):
  def toModel(characters: Set[Character]): Relationship =
    Relationship(characters, RelationshipType.withName(relationshipType), nameInFic)

object RelationshipDoc:
  def fromModel(model: Relationship): RelationshipDoc =
    RelationshipDoc(model.nameInFic.getOrElse(model.name), model.shipType.toString, model.nameInFic)
