import sbt.*

object Dependencies {
  object V {
    val telegramium   = "9.77.0"
    val scalaScraper  = "3.1.1"
    val scalaParallel = "1.0.4"
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

  val zio       = "dev.zio" %% "zio"              % V.zio
  val zioToCats = "dev.zio" %% "zio-interop-cats" % "23.1.0.2"
  val zioHttp   = "dev.zio" %% "zio-http"         % "3.0.0-RC9"
  val zioJson   = "dev.zio" %% "zio-json"         % "0.7.3"

  val telegramium: Seq[ModuleID] = Seq(
    "io.github.apimorphism" %% "telegramium-core" % V.telegramium,
    "io.github.apimorphism" %% "telegramium-high" % V.telegramium
  )

  val logback      = "ch.qos.logback"    % "logback-classic" % V.logBack
  val uri          = "io.lemonlabs"     %% "scala-uri"       % V.scalaUri
  val scalaScraper = "net.ruippeixotog" %% "scala-scraper"   % V.scalaScraper
  val javaMail     = "javax.mail"        % "mail"            % "1.4.7"

  val sqlite: Seq[ModuleID] = Seq(
    "com.typesafe.slick" %% "slick"          % "3.5.1",
    "org.slf4j"           % "slf4j-nop"      % "1.7.26",
    "org.xerial"          % "sqlite-jdbc"    % "3.34.0"
  )

  val ao3: Seq[ModuleID]         = Seq(zio, scalaParallel, scalatest, scalaScraper, uri, zioHttp)
  val kapibarabot: Seq[ModuleID] = Seq(scalaParallel, logback, scalaz, javaMail, zioJson, zioToCats) ++ telegramium ++ sqlite
}
