package com.kapibarabanka.ao3scrapper.models

case class Fandom(name: String) extends Tag:
  val category: TagCategory = TagCategory.Fandom
  
