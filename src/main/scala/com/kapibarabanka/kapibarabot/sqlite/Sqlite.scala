package com.kapibarabanka.kapibarabot.sqlite

import com.kapibarabanka.kapibarabot.sqlite.tables.MyTable
import com.kapibarabanka.kapibarabot.utils
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcBackend.{Database, JdbcDatabaseDef}
import slick.jdbc.PostgresProfile.api.*
import zio.ZIO

type Database = JdbcDatabaseDef

object Sqlite:
  private val config    = ConfigFactory.parseString("driver=org.sqlite.JDBC,connectionPool=disabled,keepAliveConnection=true")
  private val appConfig = utils.Config

  def run[T](action: DBIOAction[T, NoStream, Nothing]): ZIO[Any, Throwable, T] = {
    val url           = s"jdbc:sqlite:${appConfig.dbPath}${appConfig.dbName}"
    val configWithUrl = config.withValue("url", ConfigValueFactory.fromAnyRef(url))

    def connectToDb = {
      for {
        db <- ZIO.attempt(Database.forConfig("", configWithUrl))
        _  <- ZIO.log(s"Connected to DB $url")
      } yield db
    }

    def close(db: JdbcDatabaseDef) = for {
      _ <- ZIO.succeed(db.close())
      _ <- ZIO.log(s"Closed connection to DB $url")
    } yield ()

    def use(db: JdbcDatabaseDef) = ZIO.fromFuture { implicit ec => db.run(action) }

    ZIO.acquireReleaseWith(connectToDb)(close)(use)
  }

  def createManyToManyTable(
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
