package kapibarabanka.lo3.models
package tg

case class FicComment(commentDate: String, comment: String):
  def format() = s"$commentDate:\n$comment"
