package com.kapibarabanka.kapibarabot.domain


sealed trait ReadDates

case class StartAndFinish(startDate: String, finishDate: String) extends ReadDates
case class Start(date: String) extends ReadDates
case class SingleDayRead(date: String) extends ReadDates
