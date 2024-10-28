package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

case class ArchiveWarning(name: String) extends Tag:
  val category: TagCategory = TagCategory.Warning

object ArchiveWarning:
  implicit val schema: Schema[ArchiveWarning] = DeriveSchema.gen
