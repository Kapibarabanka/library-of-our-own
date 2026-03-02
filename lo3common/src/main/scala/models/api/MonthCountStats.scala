package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class MonthCountStats(month: String, fics: Int, words: Long)

object MonthCountStats:
  implicit val schema: Schema[MonthCountStats] = DeriveSchema.gen
