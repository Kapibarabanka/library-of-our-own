package kapibarabanka.lo3.models
package ao3

case class ArchiveWarning(name: String) extends Tag:
  val category: TagCategory = TagCategory.Warning
