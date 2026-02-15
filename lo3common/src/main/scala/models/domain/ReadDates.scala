package kapibarabanka.lo3.common
package models.domain

import java.time.LocalDateTime

case class ReadDates(id: Option[Int], startDate: LocalDateTime, finishDate: Option[LocalDateTime], isAbandoned: Boolean)
