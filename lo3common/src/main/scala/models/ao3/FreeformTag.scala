package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

case class FreeformTag(name: String) extends Tag:
  val category: TagCategory = TagCategory.Freeform

object FreeformTag:
  implicit val schema: Schema[FreeformTag] = DeriveSchema.gen
