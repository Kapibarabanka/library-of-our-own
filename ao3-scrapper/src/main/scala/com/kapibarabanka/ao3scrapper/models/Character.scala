package com.kapibarabanka.ao3scrapper.models

case class Character(name: String) extends Tag:
  val category: TagCategory = TagCategory.Character