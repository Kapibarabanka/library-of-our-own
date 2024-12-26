package kapibarabanka.lo3.common

package models.api

import models.domain.{FicDetails, FlatFicModel, UserFicKey}

import zio.schema.{DeriveSchema, Schema}

case class FicCard(
    key: UserFicKey,
    fic: FlatFicModel,
//    childCards: List[FicCard],
    details: FicDetails
)

object FicCard:
  implicit val schema: Schema[FicCard] = DeriveSchema.gen
