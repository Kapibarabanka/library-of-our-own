package kapibarabanka.lo3.bot
package sqlite.docs

import ao3scrapper.Ao3TagName
import ao3scrapper.domain.Character

case class CharacterDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Character = Character(name, label)

object CharacterDoc:
  def fromModel(model: Character): CharacterDoc =
    CharacterDoc(Ao3TagName.combineWithLabel(model.name, model.label), model.name, model.label)
