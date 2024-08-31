package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.ao3scrapper.models.FreeformTag

case class TagDoc(name: String, category: Option[String], filterable: Boolean):
  def toModel = FreeformTag(name, Some(filterable))

object TagDoc:
  def fromModel(model: FreeformTag): TagDoc = TagDoc(model.name, None, model.isFilterable.getOrElse(false))
