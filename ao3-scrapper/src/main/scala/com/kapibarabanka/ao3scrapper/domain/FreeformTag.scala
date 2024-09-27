package com.kapibarabanka.ao3scrapper.domain

case class FreeformTag(name: String, isFilterable: Option[Boolean]) extends Tag:
  val category: TagCategory = TagCategory.Freeform
