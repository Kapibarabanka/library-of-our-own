package kapibarabanka.lo3.bot
package sqlite.docs

import kapibarabanka.lo3.models.tg.{ReadDates, SingleDayRead, Start, StartAndFinish}

case class ReadDatesDoc(
    id: Option[Int],
    userId: String,
    ficId: String,
    ficIsSeries: Boolean,
    startDate: Option[String],
    endDate: Option[String]
):
  def toModel: ReadDates = (startDate, endDate) match
    case (None, Some(finish))        => SingleDayRead(finish)
    case (Some(start), None)         => Start(start)
    case (Some(start), Some(finish)) => StartAndFinish(start, finish)
    case (None, None)                => SingleDayRead("EMPTY_DATE")
