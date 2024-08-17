import sbt.*

object Dependencies {
  object V {
    val circe         = "0.14.9"
    val http4s        = "0.23.27"
    val blazeHttp4s   = "0.23.16"
    val telegramium   = "9.77.0"
    val scalaScraper  = "3.1.1"
    val scalaParallel = "1.0.4"
    val catsEffect    = "3.5.4"
    val logBack       = "1.5.6"
    val scalaUri      = "4.0.3"
    val scalatest     = "3.2.19"
    val scalaz        = "7.3.8"
    val requests      = "0.9.0"
    val zio           = "2.1.6"
  }

  val scalaParallel = "org.scala-lang.modules" %% "scala-parallel-collections" % V.scalaParallel
  val scalatest     = "org.scalatest"          %% "scalatest"                  % V.scalatest % Test

  val scalaz = "org.scalaz" %% "scalaz-core" % V.scalaz

  val catsEffect = "org.typelevel" %% "cats-effect"      % V.catsEffect
  val zio        = "dev.zio"       %% "zio"              % V.zio
  val zioToCats  = "dev.zio"       %% "zio-interop-cats" % "23.1.0.2"
  val zioHttp    = "dev.zio"       %% "zio-http"         % "3.0.0-RC9"

  val circeCore    = "io.circe" %% "circe-core"    % V.circe
  val circeParser  = "io.circe" %% "circe-parser"  % V.circe
  val circeGeneric = "io.circe" %% "circe-generic" % V.circe

  val http4sCirce = "org.http4s" %% "http4s-circe"        % V.http4s
  val http4sBlaze = "org.http4s" %% "http4s-blaze-client" % V.blazeHttp4s

  val telegramium: Seq[ModuleID] = Seq(
    "io.github.apimorphism" %% "telegramium-core" % V.telegramium,
    "io.github.apimorphism" %% "telegramium-high" % V.telegramium
  )

  val logback      = "ch.qos.logback"    % "logback-classic" % V.logBack
  val uri          = "io.lemonlabs"     %% "scala-uri"       % V.scalaUri
  val scalaScraper = "net.ruippeixotog" %% "scala-scraper"   % V.scalaScraper
  val javaMail     = "javax.mail"        % "mail"            % "1.4.7"

  val ao3: Seq[ModuleID]         = Seq(zio, scalaParallel, scalatest, scalaScraper, uri, zioHttp)
  val airtable: Seq[ModuleID]    = Seq(catsEffect, circeCore, circeGeneric, http4sCirce, http4sBlaze, zio, zioToCats)
  val kapibarabot: Seq[ModuleID] = Seq(scalaParallel, logback, scalaz, javaMail) ++ telegramium

}
