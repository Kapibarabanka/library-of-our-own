package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class TagDataset(tagValue: String, counts: List[Int])

object TagDataset:
  implicit val schema: Schema[TagDataset] = DeriveSchema.gen
