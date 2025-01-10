package kapibarabanka.lo3.common
package models.ao3

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDate

case class Series(
    id: String,
    link: String,
    title: String,
    authors: List[String] = List(),
    started: LocalDate,
    updated: Option[LocalDate],
    words: Int,
    complete: Boolean,
    bookmarks: Option[Int],
    description: Option[String],
    unsavedWorks: List[Work] = List(),
    workIds: List[String] = List()
)

object Series:
  implicit val schema: Schema[Series] = DeriveSchema.gen
