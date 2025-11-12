package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.CommentDoc
import sqlite.services.Lo3Db
import sqlite.tables.CommentsTable

import kapibarabanka.lo3.common.models.domain.{DbError, Note, UserFicKey}
import slick.jdbc.PostgresProfile.api.*
import zio.IO

import java.time.LocalDateTime

class NotesRepo(db: Lo3Db):
  private val comments = TableQuery[CommentsTable]

  def addNote(key: UserFicKey, note: Note): IO[DbError, Unit] = db
    .run(
      comments += CommentDoc(None, key.userId, key.ficId, key.ficIsSeries, note.date.toString, note.text)
    )
    .unit

  def getAllNotes(key: UserFicKey): IO[DbError, List[Note]] = for {
    comments <- db.run(filterNotes(key).result)
  } yield comments.map(_.toModel).toList.sortBy(_.date)(Ordering[LocalDateTime].reverse)

  private def filterNotes(key: UserFicKey) =
    comments.filter(d => d.userId === key.userId && d.ficId === key.ficId && d.ficIsSeries === key.ficIsSeries)
