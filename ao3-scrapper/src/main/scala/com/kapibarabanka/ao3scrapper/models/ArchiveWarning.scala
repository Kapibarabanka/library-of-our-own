package com.kapibarabanka.ao3scrapper.models

case class ArchiveWarning(name: String) extends Tag:
  val category: TagCategory = TagCategory.Warning
