package kapibarabanka.lo3.models
package ao3

import zio.schema.{DeriveSchema, Schema}

case class Work(
    id: String,
    title: String,
    authors: List[String],
    rating: Rating.Value,
    warnings: Set[ArchiveWarning],
    categories: Set[Category.Value],
    fandoms: Set[Fandom],
    relationships: List[Relationship],
    characters: Set[Character],
    freeformTags: List[FreeformTag],
    link: String,
    date: WorkDate,
    words: Int,
    chaptersWritten: Int,
    chaptersPlanned: Option[Int],
    comments: Option[Int],
    kudos: Option[Int],
    hits: Option[Int],
    bookmarks: Option[Int]
):
  val complete: Boolean = chaptersPlanned match
    case Some(planned) => chaptersWritten >= planned
    case None          => false

object Work:
  implicit val schema: Schema[Work] = DeriveSchema.gen
