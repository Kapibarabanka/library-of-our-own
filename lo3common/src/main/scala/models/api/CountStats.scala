package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class CountStats(stats: List[MonthCountStats], totalFics: Int)

object CountStats:
  implicit val schema: Schema[CountStats] = DeriveSchema.gen
