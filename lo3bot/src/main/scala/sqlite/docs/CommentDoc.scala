package kapibarabanka.lo3.bot
package sqlite.docs

import domain.FicComment

case class CommentDoc(id: Option[Int], userId: String, ficId: String, ficIsSeries: Boolean, commentDate: String, comment: String):
  def toModel: FicComment = FicComment(commentDate, comment)
