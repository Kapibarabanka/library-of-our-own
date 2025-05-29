package kapibarabanka.lo3.api
package services.ao3Info.internal

import java.time.LocalDate

trait WorkDoc {
  val title: String
  val authors: Iterable[String]
  val rating: String
  val mobiLink: Option[String]
  val warnings: List[String]
  val categories: List[String]
  val fandoms: List[String]
  val relationships: List[String]
  val characters: List[String]
  val freeformTags: List[String]
  val published: LocalDate
  val updated: Option[LocalDate]
  val words: Int
  val chaptersWritten: Option[Int]
  val chaptersPlanned: Option[Int]
}
