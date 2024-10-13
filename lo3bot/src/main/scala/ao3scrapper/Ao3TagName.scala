package kapibarabanka.lo3.bot
package ao3scrapper

import scala.util.matching.Regex

object Ao3TagName:
  private val labelPattern: Regex = """^(.*)\s\((.*)\)$""".r

  def trySeparateLabel(nameInWork: String): (String, Option[String]) =
    nameInWork match
      case labelPattern(name, label) => (name, Some(label))
      case _                         => (nameInWork, None)

  def combineWithLabel(name: String, label: Option[String]): String = label match
    case Some(value) => s"$name ($value)"
    case None        => name
