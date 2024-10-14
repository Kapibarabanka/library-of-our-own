package kapibarabanka.lo3.bot
package sqlite.docs
import kapibarabanka.lo3.models.ao3.Fandom

import kapibarabanka.lo3.models.ao3.Ao3TagName

case class FandomDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Fandom = Fandom(name, label)

object FandomDoc:
  def fromModel(model: Fandom): FandomDoc =
    FandomDoc(Ao3TagName.combineWithLabel(model.name, model.label), model.name, model.label)
