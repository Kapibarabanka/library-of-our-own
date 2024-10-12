package com.kapibarabanka.kapibarabot.sqlite.repos

import com.kapibarabanka.kapibarabot.domain.{FicComment, UserFicKey}
import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.docs.CommentDoc
import com.kapibarabanka.kapibarabot.sqlite.services.KapibarabotDb
import com.kapibarabanka.kapibarabot.sqlite.tables.CommentsTable
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class CommentsRepo(db: KapibarabotDb):
  private val comments = TableQuery[CommentsTable]

  def addComment(key: UserFicKey, comment: FicComment): IO[SqliteError, Unit] = db
    .run(
      comments += CommentDoc(None, key.userId, key.ficId, key.ficIsSeries, comment.commentDate, comment.comment)
    )
    .unit

  def getAllComments(key: UserFicKey): IO[SqliteError, List[FicComment]] = for {
    comments <- db.run(filterComments(key).result)
  } yield comments.map(_.toModel).toList.sortBy(_.commentDate)

  private def filterComments(key: UserFicKey) =
    comments.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)
