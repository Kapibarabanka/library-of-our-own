package kapibarabanka.lo3.api
package services.ao3Info.internal

import java.time.LocalDate
import scala.language.postfixOps

protected[ao3Info] object StringUtils:
  def parseDate(str: String): LocalDate = LocalDate.parse(str)
  def commaStyleToInt(str: String): Int = str.replace(",", "").toInt
