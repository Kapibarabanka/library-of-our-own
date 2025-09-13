package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class TagFieldStats(
    allLabels: Set[String],
    labelsByFics: Set[String],
    labelsByWords: Set[String],
    datasets: List[TagDataset]
)

object TagFieldStats:
  implicit val schema: Schema[TagFieldStats] = DeriveSchema.gen
