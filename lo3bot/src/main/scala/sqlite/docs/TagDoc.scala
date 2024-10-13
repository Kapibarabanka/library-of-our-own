package kapibarabanka.lo3.bot
package sqlite.docs

import ao3scrapper.domain.FreeformTag

case class TagDoc(name: String, category: Option[String], filterable: Boolean):
  def toModel = FreeformTag(name, Some(filterable))

object TagDoc:
  def fromModel(model: FreeformTag): TagDoc = TagDoc(model.name, None, model.isFilterable.getOrElse(false))
