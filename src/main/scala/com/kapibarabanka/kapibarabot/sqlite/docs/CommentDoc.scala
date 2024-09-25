package com.kapibarabanka.kapibarabot.sqlite.docs

import com.kapibarabanka.kapibarabot.domain.FicComment

case class CommentDoc(id: Option[Int], userId: String, ficId: String, ficIsSeries: Boolean, commentDate: String, comment: String):
  def toModel: FicComment = FicComment(commentDate, comment)
