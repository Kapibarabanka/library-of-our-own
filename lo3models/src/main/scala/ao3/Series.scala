package kapibarabanka.lo3.models
package ao3

import java.time.LocalDate

case class Series(
    id: String,
    link: String,
    title: String,
    authors: List[String],
    started: LocalDate,
    updated: Option[LocalDate],
    words: Int,
    complete: Boolean,
    bookmarks: Option[Int],
    description: Option[String],
    works: List[Work]
):
  val rating: Rating.Value = works.map(_.rating).maxBy(_.id)
  val warnings: Set[ArchiveWarning] = works.flatMap(_.warnings).toSet
  val categories: Set[Category.Value] = works.flatMap(_.categories).toSet
  val fandoms: Set[Fandom] = works.flatMap(_.fandoms).toSet
  val relationships: List[Relationship] = works.flatMap(_.relationships).distinct
  val characters: Set[Character] = works.flatMap(_.characters).toSet
  val freeformTags: List[FreeformTag] = works.flatMap(_.freeformTags).distinct
