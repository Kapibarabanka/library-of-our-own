package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

import java.time.LocalDateTime

case class Note(id: Option[Int], date: LocalDateTime, text: String):
  def format() = s"$date:\n$text"

object Note:
  implicit val schema: Schema[Note] = DeriveSchema.gen
