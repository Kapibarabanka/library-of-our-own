package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.domain.FicComment
import com.kapibarabanka.kapibarabot.sqlite.WithDb
import com.kapibarabanka.kapibarabot.sqlite.docs.CommentDoc
import com.kapibarabanka.kapibarabot.sqlite.tables.CommentsTable
import slick.jdbc.PostgresProfile.api.*
import zio.IO

class CommentsRepo(userId: String) extends WithDb(userId):
  private val comments = TableQuery[CommentsTable]

  def add(ficId: String, ficIsSeries: Boolean, comment: FicComment): IO[Throwable, Unit] = db(
    comments += CommentDoc(None, ficId, ficIsSeries, comment.commentDate, comment.comment)
  ).unit

  def getAllComments(ficId: String, ficIsSeries: Boolean): IO[Throwable, List[FicComment]] = for {
    comments <- db(comments.filter(c => c.ficId === ficId && c.ficIsSeries === ficIsSeries).result)
  } yield comments.map(_.toModel).toList.sortBy(_.commentDate)
