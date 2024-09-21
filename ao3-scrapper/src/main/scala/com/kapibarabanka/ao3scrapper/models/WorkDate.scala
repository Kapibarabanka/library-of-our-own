package com.kapibarabanka.ao3scrapper.models

import java.time.LocalDate

sealed trait WorkDate

case class Published(date: LocalDate)                                    extends WorkDate
case class PublishedAndUpdated(published: LocalDate, updated: LocalDate) extends WorkDate
// if work was parsed from the series there is no way to tell whether the date in the corner is "published" date or "updated" date
case class SingleDate(date: LocalDate) extends WorkDate
