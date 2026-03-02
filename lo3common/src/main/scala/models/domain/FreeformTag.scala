package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

case class FreeformTag(nameInWork: String, canonicalName: String)

object FreeformTag:
  implicit val schema: Schema[FreeformTag] = DeriveSchema.gen
