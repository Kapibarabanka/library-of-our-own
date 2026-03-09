package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

case class ReadDates(id: Option[Int] = None, startDate: LocalDateTime, finishDate: Option[LocalDateTime], isAbandoned: Boolean)

object ReadDates:
  implicit val schema: Schema[ReadDates] = DeriveSchema.gen
