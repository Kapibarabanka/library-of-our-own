package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

case class ReadDatesInfo(
    readDates: List[ReadDates] = List(),
    canStart: Boolean,
    canFinish: Boolean
):
  val alreadyRead: Boolean = readDates.exists(d => d.finishDate.nonEmpty)

object ReadDatesInfo:
  implicit val schema: Schema[ReadDatesInfo] = DeriveSchema.gen
  def fromDates(dates: Seq[ReadDates]): ReadDatesInfo =
    val sortedDates = dates.toList.sortBy(_.startDate)(Ordering[LocalDateTime].reverse)
    val maybeHead   = sortedDates.headOption
    val canStart = maybeHead match
      case None       => true
      case Some(date) => date.finishDate.isDefined
    val canFinish = maybeHead match
      case None       => false
      case Some(date) => date.finishDate.isEmpty
    ReadDatesInfo(sortedDates, canStart, canFinish)
