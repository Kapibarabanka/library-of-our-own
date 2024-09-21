package com.kapibarabanka.ao3scrapper.models

import com.kapibarabanka.ao3scrapper.StringUtils

case class Fandom(name: String, label: Option[String]) extends Tag:
  val category: TagCategory = TagCategory.Fandom

object Fandom:
  def fromNameInWork(nameInWork: String) =
    val (name, label) = StringUtils.trySeparateLabel(nameInWork)
    Fandom(name, label)
