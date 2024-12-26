package kapibarabanka.lo3.common
package models.api

import zio.schema.{DeriveSchema, Schema}

case class FicsPage(total: Int, cards: List[FicCard])

object FicsPage:
  implicit val schema: Schema[FicsPage] = DeriveSchema.gen
