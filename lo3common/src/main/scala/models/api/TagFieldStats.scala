package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class TagFieldStats(
    allLabels: Set[String],
    topByFics: List[TagDataPoint],
    topByWords: List[TagDataPoint],
    datasets: List[TagDataset]
)

object TagFieldStats:
  implicit val schema: Schema[TagFieldStats] = DeriveSchema.gen
