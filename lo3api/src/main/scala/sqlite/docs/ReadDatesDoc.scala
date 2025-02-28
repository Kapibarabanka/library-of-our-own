package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.domain.{Abandoned, ReadDates, SingleDayRead, Start, StartAndFinish}

case class ReadDatesDoc(
    id: Option[Int],
    userId: String,
    ficId: String,
    ficIsSeries: Boolean,
    startDate: Option[String],
    endDate: Option[String],
    isAbandoned: Boolean
):
  def toModel: ReadDates = (startDate, endDate) match
    case (None, Some(finish))        => if (isAbandoned) Abandoned(finish, finish) else SingleDayRead(finish)
    case (Some(start), None)         => Start(start)
    case (Some(start), Some(finish)) => if (isAbandoned) Abandoned(start, finish) else StartAndFinish(start, finish)
    case (None, None)                => SingleDayRead("EMPTY_DATE")
