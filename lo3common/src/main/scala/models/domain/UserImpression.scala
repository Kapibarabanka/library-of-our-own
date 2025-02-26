package kapibarabanka.lo3.common
package models.domain

import zio.schema.Schema

object UserImpression extends Enumeration {
  type UserImpression = Value
  val Never     = Value("Never again")
  val Meh       = Value("Meh")
  val Ok        = Value("Ok")
  val Nice      = Value("Nice")
  val Brilliant = Value("Brilliant")

  implicit val schema: Schema[UserImpression.Value] = Schema
    .primitive[String]
    .transform[UserImpression.Value](
      s => UserImpression.withName(s),
      quality => quality.toString
    )
}
