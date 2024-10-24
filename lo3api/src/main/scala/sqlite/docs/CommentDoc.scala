package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.models.tg.FicComment

case class CommentDoc(id: Option[Int], userId: String, ficId: String, ficIsSeries: Boolean, commentDate: String, comment: String):
  def toModel: FicComment = FicComment(commentDate, comment)