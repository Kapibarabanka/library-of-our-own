package kapibarabanka.lo3.common
package models.api

import models.domain.UserFicKey
import models.domain.UserImpression.UserImpression

import zio.schema.{DeriveSchema, Schema}

case class FinishInfo(
    key: UserFicKey,
    abandoned: Boolean,
    spicy: Boolean,
    impression: Option[UserImpression],
    note: Option[String]
)

object FinishInfo:
  implicit val schema: Schema[FinishInfo] = DeriveSchema.gen
