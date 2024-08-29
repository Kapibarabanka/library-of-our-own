package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.StringUtils
import com.kapibarabanka.ao3scrapper.models.Fandom

case class FandomDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Fandom = Fandom(name, label)

object FandomDoc:
  def fromModel(model: Fandom): FandomDoc =
    FandomDoc(StringUtils.combineWithLabel(model.name, model.label), model.name, model.label)
