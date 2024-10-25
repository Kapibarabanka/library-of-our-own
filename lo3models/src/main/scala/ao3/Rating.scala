package kapibarabanka.lo3.models
package ao3

import zio.schema.Schema

object Rating extends Enumeration {
  type Rating = Value
  val None     = Value("Not Rated")
  val General  = Value("General Audiences")
  val Teen     = Value("Teen And Up Audiences")
  val Mature   = Value("Mature")
  val Explicit = Value("Explicit")

  implicit val schema: Schema[Rating.Value] = Schema
    .primitive[String]
    .transform[Rating.Value](
      s => Rating.withName(s),
      rating => rating.toString
    )
}
