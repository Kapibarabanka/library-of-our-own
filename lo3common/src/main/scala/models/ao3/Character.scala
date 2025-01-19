package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

case class Character(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Character
  val fullName: String = Ao3TagName.combineWithLabel(name, label)

object Character:
  def fromNameInWork(nameInWork: String): Character =
    val (name, label) = Ao3TagName.trySeparateLabel(nameInWork)
    Character(name, label)
  implicit val schema: Schema[Character] = DeriveSchema.gen
