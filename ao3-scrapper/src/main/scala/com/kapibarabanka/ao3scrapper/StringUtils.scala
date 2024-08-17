package com.kapibarabanka.ao3scrapper

import java.util.Date
import scala.language.postfixOps

object StringUtils {
  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
  def parseDate(str: String): Date = dateFormat.parse(str)
  def commaStyleToLong(str: String): Long = str.replace(",", "").toLong
}
