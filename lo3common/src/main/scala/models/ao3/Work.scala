package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

case class Work(
    id: String,
    title: String,
    authors: List[String] = List(),
    rating: Rating.Value,
    warnings: Set[ArchiveWarning] = Set(),
    categories: Set[Category.Value] = Set(),
    fandoms: Set[Fandom] = Set(),
    relationships: List[Relationship] = List(),
    parsedShips: List[String] = List(),
    characters: Set[Character] = Set(),
    freeformTags: List[FreeformTag] = List(),
    link: String,
    date: WorkDate,
    words: Int,
    chaptersWritten: Int,
    chaptersPlanned: Option[Int],
    comments: Option[Int],
    kudos: Option[Int],
    hits: Option[Int],
    bookmarks: Option[Int],
):
  val complete: Boolean = chaptersPlanned match
    case Some(planned) => chaptersWritten >= planned
    case None          => false

object Work:
  implicit val schema: Schema[Work] = DeriveSchema.gen
