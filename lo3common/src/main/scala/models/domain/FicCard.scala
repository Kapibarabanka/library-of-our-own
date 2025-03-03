package kapibarabanka.lo3.common
package models.domain

import models.domain.{FicDetails, Ao3FicInfo, UserFicKey}

import zio.schema.{DeriveSchema, Schema}

case class FicCard(
    key: UserFicKey,
    ao3Info: Ao3FicInfo,
    details: FicDetails
)

object FicCard:
  implicit val schema: Schema[FicCard] = DeriveSchema.gen
