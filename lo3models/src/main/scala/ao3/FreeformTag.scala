package kapibarabanka.lo3.models
package ao3

case class FreeformTag(name: String, isFilterable: Option[Boolean]) extends Tag:
  val category: TagCategory = TagCategory.Freeform
