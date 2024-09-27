package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.domain.Fandom
import com.kapibarabanka.ao3scrapper.Ao3TagName

case class FandomDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Fandom = Fandom(name, label)

object FandomDoc:
  def fromModel(model: Fandom): FandomDoc =
    FandomDoc(Ao3TagName.combineWithLabel(model.name, model.label), model.name, model.label)
