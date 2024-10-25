package kapibarabanka.lo3.models
package tg

import zio.schema.{DeriveSchema, Schema}

case class FicComment(commentDate: String, comment: String):
  def format() = s"$commentDate:\n$comment"

object FicComment:
  implicit val schema: Schema[FicComment] = DeriveSchema.gen