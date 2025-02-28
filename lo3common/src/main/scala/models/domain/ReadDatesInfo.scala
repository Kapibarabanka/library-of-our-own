package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

case class ReadDatesInfo(
    readDates: List[ReadDates] = List(),
    canStart: Boolean,
    canFinish: Boolean,
):
  val finishedReading: Boolean = readDates.exists(d =>
    d match
      case StartAndFinish(startDate, finishDate) => true
      case Start(date)                           => false
      case SingleDayRead(date)                   => true
      case Abandoned(_, _)                       => true
  )

object ReadDatesInfo:
  implicit val schema: Schema[ReadDatesInfo] = DeriveSchema.gen
