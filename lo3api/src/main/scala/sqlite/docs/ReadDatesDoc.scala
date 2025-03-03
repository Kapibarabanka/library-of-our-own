package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.domain.ReadDates

import java.time.LocalDate

case class ReadDatesDoc(
    id: Option[Int],
    userId: String,
    ficId: String,
    ficIsSeries: Boolean,
    startDate: Option[String],
    endDate: Option[String],
    isAbandoned: Boolean
):
  def toModel: ReadDates =
    ReadDates(id, LocalDate.parse(startDate.getOrElse(endDate.get)), endDate.map(LocalDate.parse(_)), isAbandoned)
