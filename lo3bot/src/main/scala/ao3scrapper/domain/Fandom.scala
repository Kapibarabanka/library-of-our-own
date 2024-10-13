package kapibarabanka.lo3.bot
package ao3scrapper.domain

import ao3scrapper.Ao3TagName

case class Fandom(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Fandom

object Fandom:
  def fromNameInWork(nameInWork: String): Fandom =
    val (name, label) = Ao3TagName.trySeparateLabel(nameInWork)
    Fandom(name, label)
