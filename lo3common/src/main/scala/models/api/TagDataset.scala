package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class TagDataset(timeLabel: String, byFics: List[TagDataPoint], byWords: List[TagDataPoint])

object TagDataset:
  implicit val schema: Schema[TagDataset] = DeriveSchema.gen
