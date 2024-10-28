package kapibarabanka.lo3.common
package models.domain

import zio.schema.{DeriveSchema, Schema}

case class FicComment(commentDate: String, comment: String):
  def format() = s"$commentDate:\n$comment"

object FicComment:
  implicit val schema: Schema[FicComment] = DeriveSchema.gen