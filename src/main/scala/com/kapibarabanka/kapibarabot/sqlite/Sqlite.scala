package com.kapibarabanka.kapibarabot.sqlite

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcBackend.{Database, JdbcDatabaseDef}
import zio.ZIO

type Database = JdbcDatabaseDef

object Sqlite:
  private val config     = ConfigFactory.parseString("driver=org.sqlite.JDBC,connectionPool=disabled,keepAliveConnection=true")
  private val dbFilePath = sys.env("SQLITE_DB")

  def run[T](userId: String)(action: DBIOAction[T, NoStream, Nothing]): ZIO[Any, Throwable, T] = {
    val url           = s"jdbc:sqlite:${dbFilePath}ao3_sqlite_$userId.db"
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