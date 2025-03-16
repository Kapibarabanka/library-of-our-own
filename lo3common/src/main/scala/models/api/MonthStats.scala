package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class MonthStats(month: String, fics: Int, words: Int)

object MonthStats:
  implicit val schema: Schema[MonthStats] = DeriveSchema.gen
