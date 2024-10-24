package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.SqliteError
import sqlite.docs.CommentDoc
import sqlite.services.KapibarabotDb
import sqlite.tables.CommentsTable

import kapibarabanka.lo3.models.tg.{FicComment, UserFicKey}
import slick.jdbc.PostgresProfile.api.*
import zio.IO

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
