package kapibarabanka.lo3.api
package ficService.internal

import java.time.LocalDate
import scala.language.postfixOps

protected[ficService] object StringUtils:
  def parseDate(str: String): LocalDate = LocalDate.parse(str)
  def commaStyleToInt(str: String): Int = str.replace(",", "").toInt
