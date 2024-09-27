package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.domain.Character
import com.kapibarabanka.ao3scrapper.Ao3TagName

case class CharacterDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Character = Character(name, label)

object CharacterDoc:
  def fromModel(model: Character): CharacterDoc =
    CharacterDoc(Ao3TagName.combineWithLabel(model.name, model.label), model.name, model.label)
