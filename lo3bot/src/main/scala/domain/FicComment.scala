package kapibarabanka.lo3.bot
package domain

case class FicComment(commentDate: String, comment: String):
  def format() = s"$commentDate:\n$comment"
