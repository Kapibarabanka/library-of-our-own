package com.kapibarabanka.kapibarabot.sqlite.services

import com.kapibarabanka.kapibarabot.sqlite.SqliteError
import com.kapibarabanka.kapibarabot.sqlite.tables.*
import com.kapibarabanka.kapibarabot.sqlite.SqliteError.*
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcBackend.{Database, JdbcDatabaseDef}
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO, ZLayer}

class KapibarabotDb(dbWithPath: String):
  private val config = ConfigFactory.parseString("driver=org.sqlite.JDBC,connectionPool=disabled,keepAliveConnection=true")

  val allTables: List[MyTable] = List(
    UsersTable,
    WorksTable,
    SeriesTable,
    FicsDetailsTable,
    SeriesToWorksTable,
    FandomsTable,
    WorksToFandomsTable,
    CharactersTable,
    WorksToCharactersTable,
    RelationshipsTable,
    ShipsToCharactersTable,
    WorksToShipsTable,
    TagsTable,
    WorksToTagsTable,
    CommentsTable,
    ReadDatesTable
  )

  def init: IO[SqliteError, Unit] = for {
    _ <- run(DBIO.sequence(allTables.map(_.createIfNotExists))).mapError(DbInitError(_))
  } yield ()

  def run[T](action: DBIOAction[T, NoStream, Nothing]): ZIO[Any, SqliteError, T] = {
    val url           = s"jdbc:sqlite:$dbWithPath"
    val configWithUrl = config.withValue("url", ConfigValueFactory.fromAnyRef(url))

    def connectToDb = (for {
      db <- ZIO.attempt(Database.forConfig("", configWithUrl))
      _  <- ZIO.log(s"Connected to DB $url")
    } yield db).mapError(CantConnectToDb(_))

    def close(db: JdbcDatabaseDef) = for {
      _ <- ZIO.succeed(db.close())
      _ <- ZIO.log(s"Closed connection to DB $url")
    } yield ()

    def use(db: JdbcDatabaseDef) = ZIO.fromFuture { implicit ec => db.run(action) } mapError (DbActionError(_))

    ZIO.acquireReleaseWith(connectToDb)(close)(use)
  }

object KapibarabotDb:
  def createManyToManyTableAction(
      name: String,
      leftFieldName: String,
      leftTable: MyTable,
      rightFieldName: String,
      rightTable: MyTable
  ) =
    sqlu"""
        CREATE TABLE IF NOT EXISTS "#$name" (
        "id"	INTEGER NOT NULL UNIQUE,
        "#$leftFieldName"	TEXT NOT NULL,
        "#$rightFieldName"	TEXT NOT NULL,
        PRIMARY KEY("id"),
        FOREIGN KEY("#$leftFieldName") REFERENCES "#${leftTable.name}"("#${leftTable.keyField}") ON UPDATE CASCADE ON DELETE CASCADE,
        FOREIGN KEY("#$rightFieldName") REFERENCES "#${rightTable.name}"("#${rightTable.keyField}") ON UPDATE CASCADE ON DELETE CASCADE);
        """
