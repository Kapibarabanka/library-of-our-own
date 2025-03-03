package kapibarabanka.lo3.api
package sqlite.docs

import kapibarabanka.lo3.common.models.domain.Note

import java.time.LocalDate

case class CommentDoc(id: Option[Int], userId: String, ficId: String, ficIsSeries: Boolean, commentDate: String, comment: String):
  def toModel: Note = Note(id, LocalDate.parse(commentDate), comment)
