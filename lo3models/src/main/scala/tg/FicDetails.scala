package kapibarabanka.lo3.models
package tg

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

case class FicDetails(
    backlog: Boolean,
    isOnKindle: Boolean,
    quality: Option[Quality.Value],
    fire: Boolean,
    recordCreated: LocalDate
)
object FicDetails:
  implicit val schema: Schema[FicDetails] = DeriveSchema.gen
