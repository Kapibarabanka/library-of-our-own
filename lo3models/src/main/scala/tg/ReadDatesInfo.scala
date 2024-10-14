package kapibarabanka.lo3.models
package tg

case class ReadDatesInfo(
    readDates: List[ReadDates],
    canAddStart: Boolean,
    canAddFinish: Boolean,
    canCancelStart: Boolean,
    canCancelFinish: Boolean
):
  val finishedReading: Boolean = readDates.exists(d =>
    d match
      case StartAndFinish(startDate, finishDate) => true
      case Start(date)                           => false
      case SingleDayRead(date)                   => true
  )
