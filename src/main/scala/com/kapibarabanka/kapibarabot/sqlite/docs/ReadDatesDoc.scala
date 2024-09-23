package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.kapibarabot.domain.{ReadDates, SingleDayRead, Start, StartAndFinish}

case class ReadDatesDoc(id: Option[Int], ficId: String, ficIsSeries: Boolean, startDate: Option[String], endDate: Option[String]):
  def toModel: ReadDates = (startDate, endDate) match
    case (None, Some(finish))        => SingleDayRead(finish)
    case (Some(start), None)         => Start(start)
    case (Some(start), Some(finish)) => StartAndFinish(start, finish)
    case (None, None)                => SingleDayRead("EMPTY_DATE")
