package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

case class Fandom(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Fandom

object Fandom:
  def fromNameInWork(nameInWork: String): Fandom =
    val (name, label) = Ao3TagName.trySeparateLabel(nameInWork)
    Fandom(name, label)

  implicit val schema: Schema[Fandom] = DeriveSchema.gen
  