package kapibarabanka.lo3.api
package ao3scrapper.internal

import java.time.LocalDate
import scala.language.postfixOps

protected[ao3scrapper] object StringUtils:
  def parseDate(str: String): LocalDate = LocalDate.parse(str)
  def commaStyleToInt(str: String): Int = str.replace(",", "").toInt
