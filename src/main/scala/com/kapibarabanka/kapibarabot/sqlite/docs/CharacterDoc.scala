package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.domain.Character
import com.kapibarabanka.ao3scrapper.utils.StringUtils

case class CharacterDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Character = Character(name, label)

object CharacterDoc:
  def fromModel(model: Character): CharacterDoc =
    CharacterDoc(StringUtils.combineWithLabel(model.name, model.label), model.name, model.label)
