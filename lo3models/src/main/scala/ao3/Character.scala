package kapibarabanka.lo3.models
package ao3

case class Character(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Character

object Character:
  def fromNameInWork(nameInWork: String): Character =
    val (name, label) = Ao3TagName.trySeparateLabel(nameInWork)
    Character(name, label)
