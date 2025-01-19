package kapibarabanka.lo3.api
package sqlite.repos

import sqlite.docs.*
import sqlite.services.Lo3Db
import sqlite.tables.*

import kapibarabanka.lo3.common.models.domain.DbError
import slick.jdbc.PostgresProfile.api.*
import scalaz.Scalaz.ToIdOps
import zio.ZIO

import scala.collection.immutable.Iterable

class TagsRepo(db: Lo3Db):
  private val canonicalTags = TableQuery[CanonicalTagsTable]

  def addCanonical(nameInWork: String, canonicalName: String, filterable: Boolean): ZIO[Any, DbError, Unit] =
    val values = s"(\"${nameInWork |> formatForSql}\", \"${canonicalName |> formatForSql}\", $filterable)"
    db.run(
      sqlu"INSERT OR IGNORE INTO #${CanonicalTagsTable.name} (nameInWork, canonicalName, filterable) VALUES #$values"
    ).unit

  def tryGetCanonical(nameInWork: String): ZIO[Any, DbError, Option[String]] =
    val value = s"\"${formatForSql(nameInWork)}\""
    db.run(
      sql"select t.canonicalName from #${CanonicalTagsTable.name} as t where t.nameInWork = #$value".as[String]
    ).map(_.headOption)

  def addTags(tags: Iterable[TagDoc]) =
    if (tags.isEmpty) DBIO.successful({})
    else
      val values = tags.map(t => s"(\"${t.name |> formatForSql}\", NULL)").mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${TagsTable.name} (name, category) VALUES #$values"

  def addFandoms(fandoms: Iterable[FandomDoc]) =
    if (fandoms.isEmpty) DBIO.successful({})
    else
      val values = fandoms
        .map(f =>
          s"(\"${f.fullName |> formatForSql}\", \"${f.name}\", ${f.label.fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
        )
        .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${FandomsTable.name} (fullName, name, label) VALUES #$values"

  def addCharacters(characters: Iterable[CharacterDoc]) =
    if (characters.isEmpty) DBIO.successful({})
    else
      val values =
        characters
          .map(c =>
            s"(\"${c.fullName |> formatForSql}\", \"${c.name |> formatForSql}\", ${c.label
                .fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
          )
          .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${CharactersTable.name} (fullName, name, label) VALUES #$values"

  def addRelationships(ships: Iterable[RelationshipDoc]) =
    if (ships.isEmpty) DBIO.successful({})
    else
      val values =
        ships
          .map(r =>
            s"(\"${r.name |> formatForSql}\", \"${r.relationshipType}\", ${r.nameInFic.fold("NULL")(l => s"\"${l |> formatForSql}\"")})"
          )
          .mkString(", ")
      sqlu"INSERT OR IGNORE INTO #${RelationshipsTable.name} (name, relationshipType, nameInFic) VALUES #$values"

  private def formatForSql(str: String) = str.replace("\"", "\"\"")
