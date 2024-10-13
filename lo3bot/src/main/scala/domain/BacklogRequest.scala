package kapibarabanka.lo3.bot
package domain

import zio.json.*

case class BacklogRequest(
    specialTags: Set[String],
    fics: List[FicJsonModel]
)

object BacklogRequest:
  implicit val encoder: JsonEncoder[BacklogRequest] = DeriveJsonEncoder.gen[BacklogRequest]
  def fromRecords(records: List[UserFicRecord]): BacklogRequest = BacklogRequest(
    specialTags = records.flatMap(_.specialTags).toSet,
    fics = records.map(FicJsonModel.fromRecord)
  )
