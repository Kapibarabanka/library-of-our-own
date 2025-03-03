package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

case class Fic(
    userId: String,
    ao3Info: Ao3FicInfo,
    readDatesInfo: ReadDatesInfo,
    notes: List[Note] = List(),
    details: FicDetails
):
  val key: UserFicKey = UserFicKey(userId, ao3Info.id, ao3Info.ficType)

  // for html backlog
  val specialTags: List[String] = List(
    if (details.isOnKindle) Some("On Kindle") else None,
    if (readDatesInfo.alreadyRead) Some("Already Read") else None,
    if (details.spicy) Some("Has Fire") else None
  ).flatten

object Fic:
  implicit val schema: Schema[Fic] = DeriveSchema.gen
