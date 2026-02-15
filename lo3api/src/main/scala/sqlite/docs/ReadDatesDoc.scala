package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.domain.ReadDates
import kapibarabanka.lo3.common.services.Utils

import java.time.LocalDateTime

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
    ReadDates(id, Utils.parseDateTime(startDate.getOrElse(endDate.get), id), endDate.map(Utils.parseDateTime(_, id)), isAbandoned)
