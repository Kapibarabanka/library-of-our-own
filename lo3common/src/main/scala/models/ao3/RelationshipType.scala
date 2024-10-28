package kapibarabanka.lo3.common
package models.ao3

import zio.schema.Schema

object RelationshipType extends Enumeration {
  type Category = Value
  val Romantic = Value("Romantic")
  val Platonic = Value("Platonic")

  implicit val schema: Schema[RelationshipType.Value] = Schema
    .primitive[String]
    .transform[RelationshipType.Value](
      s => RelationshipType.withName(s),
      shipType => shipType.toString
    )
}
