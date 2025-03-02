package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

case class FicDetails(
                       backlog: Boolean,
                       isOnKindle: Boolean,
                       impression: Option[UserImpression.Value],
                       spicy: Boolean,
                       recordCreated: LocalDateTime
)
object FicDetails:
  implicit val schema: Schema[FicDetails] = DeriveSchema.gen
