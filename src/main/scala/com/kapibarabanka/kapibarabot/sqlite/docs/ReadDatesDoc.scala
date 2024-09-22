package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.kapibarabot.domain.ReadDates

case class ReadDatesDoc(id: Option[Int], ficId: String, ficIsSeries: Boolean, startDate: Option[String], endDate: Option[String]):
  def toModel = ReadDates(startDate, endDate)
