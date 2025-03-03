package kapibarabanka.lo3.common
package models.domain

import java.time.LocalDate

case class ReadDates(id: Option[Int], startDate: LocalDate, finishDate: Option[LocalDate], isAbandoned: Boolean)
