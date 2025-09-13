package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class TagDataPoint (label: String, value: Int)
object  TagDataPoint:
  implicit val schema: Schema[TagDataPoint] = DeriveSchema.gen

