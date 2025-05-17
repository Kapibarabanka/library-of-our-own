package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class TagFieldStats(months: List[String], byFics: List[TagDataset], byWords: List[TagDataset])

object TagFieldStats:
  implicit val schema: Schema[TagFieldStats] = DeriveSchema.gen
