package kapibarabanka.lo3.models
package ao3

import zio.schema.{DeriveSchema, Schema}

case class FreeformTag(name: String, isFilterable: Option[Boolean]) extends Tag:
  val category: TagCategory = TagCategory.Freeform

object FreeformTag:
  implicit val schema: Schema[FreeformTag] = DeriveSchema.gen
