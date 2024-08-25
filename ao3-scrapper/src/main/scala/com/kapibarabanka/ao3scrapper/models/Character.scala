package com.kapibarabanka.ao3scrapper.models

import com.kapibarabanka.ao3scrapper.StringUtils

case class Character(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Character
  
object Character:
  def fromNameInWork(nameInWork: String) =
    val (name, label) = StringUtils.trySeparateLabel(nameInWork)
    Character(name, label)
