package kapibarabanka.lo3.bot
package domain

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
