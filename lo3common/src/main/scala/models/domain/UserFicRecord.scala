package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

case class UserFicRecord(
    userId: String,
    fic: FlatFicModel,
    readDatesInfo: ReadDatesInfo,
    comments: List[FicComment] = List(),
    details: FicDetails
):
  val key: UserFicKey = UserFicKey(userId, fic.id, fic.ficType)
  val specialTags: List[String] = List(
    if (details.isOnKindle) Some("On Kindle") else None,
    if (readDatesInfo.finishedReading) Some("Already Read") else None,
    if (details.fire) Some("Has Fire") else None
  ).flatten

object UserFicRecord:
  implicit val schema: Schema[UserFicRecord] = DeriveSchema.gen
