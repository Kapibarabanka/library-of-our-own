package kapibarabanka.lo3.common
package models.domain

import zio.schema.Schema

object Quality extends Enumeration {
  type Quality = Value
  val Never     = Value("Never again")
  val Meh       = Value("Meh")
  val Ok        = Value("Ok")
  val Nice      = Value("Nice")
  val Brilliant = Value("Brilliant")

  implicit val schema: Schema[Quality.Value] = Schema
    .primitive[String]
    .transform[Quality.Value](
      s => Quality.withName(s),
      quality => quality.toString
    )
}
