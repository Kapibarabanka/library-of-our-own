package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

case class ReadDatesInfo(
    readDates: List[ReadDates] = List(),
    canStart: Boolean,
    canFinish: Boolean
):
  val alreadyRead: Boolean = readDates.exists(d => d.finishDate.nonEmpty)

object ReadDatesInfo:
  implicit val schema: Schema[ReadDatesInfo] = DeriveSchema.gen
