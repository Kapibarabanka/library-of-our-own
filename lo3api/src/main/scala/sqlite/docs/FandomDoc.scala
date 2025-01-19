package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.ao3.{Ao3TagName, Fandom}

case class FandomDoc(fullName: String, name: String, label: Option[String]):
  def toModel: Fandom = Fandom(name, label)

object FandomDoc:
  def fromModel(model: Fandom): FandomDoc =
    FandomDoc(model.fullName, model.name, model.label)
