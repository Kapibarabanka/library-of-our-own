package kapibarabanka.lo3.bot
package sqlite.docs
import kapibarabanka.lo3.models.ao3.Character

import kapibarabanka.lo3.models.ao3.Ao3TagName

case class CharacterDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Character = Character(name, label)

object CharacterDoc:
  def fromModel(model: Character): CharacterDoc =
    CharacterDoc(Ao3TagName.combineWithLabel(model.name, model.label), model.name, model.label)
