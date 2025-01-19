package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.ao3.FreeformTag

case class TagDoc(name: String, category: Option[String]):
  val toModel: FreeformTag = FreeformTag(name)

object TagDoc:
  def fromModel(model: FreeformTag): TagDoc = TagDoc(model.name, None)
