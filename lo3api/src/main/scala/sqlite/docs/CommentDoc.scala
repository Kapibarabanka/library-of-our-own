package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.domain.Note
import kapibarabanka.lo3.common.services.Utils

case class CommentDoc(id: Option[Int], userId: String, ficId: String, ficIsSeries: Boolean, commentDate: String, comment: String):
  def toModel: Note = Note(id, Utils.parseDateTime(commentDate, id), comment)
