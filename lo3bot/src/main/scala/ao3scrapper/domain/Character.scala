package kapibarabanka.lo3.bot
package ao3scrapper.domain

import ao3scrapper.Ao3TagName

case class Character(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Character

object Character:
  def fromNameInWork(nameInWork: String): Character =
    val (name, label) = Ao3TagName.trySeparateLabel(nameInWork)
    Character(name, label)
