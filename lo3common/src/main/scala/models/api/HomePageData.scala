package kapibarabanka.lo3.common
package models.api

import models.domain.FicCard

import zio.schema.{DeriveSchema, Schema}

case class HomePageData(
    currentlyReading: List[FicCard],
    randomFicFromBacklog: Option[FicCard]
)

object HomePageData:
  implicit val schema: Schema[HomePageData] = DeriveSchema.gen
