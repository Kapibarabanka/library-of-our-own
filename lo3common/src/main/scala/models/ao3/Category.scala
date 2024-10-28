package kapibarabanka.lo3.common
package models.ao3

import zio.schema.Schema

object Category extends Enumeration {
  type Category = Value
  val FF    = Value("F/F")
  val FM    = Value("F/M")
  val Gen   = Value("Gen")
  val MM    = Value("M/M")
  val Multi = Value("Multi")
  val Other = Value("Other")
  val None  = Value("No category")

  implicit val schema: Schema[Category.Value] = Schema
    .primitive[String]
    .transform[Category.Value](
      s => Category.withName(s),
      category => category.toString
    )
}
