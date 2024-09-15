package com.kapibarabanka.ao3scrapper

import java.time.LocalDate
import scala.language.postfixOps
import scala.util.matching.Regex

object StringUtils {
  val dateFormat                        = new java.text.SimpleDateFormat("yyyy-MM-dd")
  def parseDate(str: String): LocalDate = LocalDate.parse(str)

  private val labelPattern: Regex = """^(.*)\s\((.*)\)$""".r
  def trySeparateLabel(nameInWork: String): (String, Option[String]) =
    nameInWork match
      case labelPattern(name, label) => (name, Some(label))
      case _                         => (nameInWork, None)

  def combineWithLabel(name: String, label: Option[String]): String = label match
    case Some(value) => s"$name ($value)"
    case None        => name

  def commaStyleToInt(str: String): Int = str.replace(",", "").toInt
}
