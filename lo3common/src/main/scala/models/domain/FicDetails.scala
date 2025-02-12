package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

case class FicDetails(
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[Quality.Value],
    spicy: Boolean,
    recordCreated: LocalDate
)
object FicDetails:
  implicit val schema: Schema[FicDetails] = DeriveSchema.gen
