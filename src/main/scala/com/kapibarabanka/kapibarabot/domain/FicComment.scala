package com.kapibarabanka.kapibarabot.domain

case class FicComment(commentDate: String, comment: String):
  def format() = s"$commentDate:\n$comment"
