import sbt.*

object Dependencies {
  object V {
    val telegramium = "9.77.0"
    val zioSchema   = "1.5.0"
  }

  object scala {
    val test               = "org.scalatest"          %% "scalatest"                  % "3.2.19" % Test
    val parallel           = "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
    val scalaz             = "org.scalaz"             %% "scalaz-core"                % "7.3.8"
    val all: Seq[ModuleID] = Seq(test, parallel, scalaz)
  }

  object zio {
    val zio       = "dev.zio" %% "zio"              % "2.1.6"
    val zioToCats = "dev.zio" %% "zio-interop-cats" % "23.1.0.2"
    val zioHttp   = "dev.zio" %% "zio-http"         % "3.0.1"
    val zioJson   = "dev.zio" %% "zio-json"         % "0.7.3"
    val zioSchema: Seq[ModuleID] = Seq(
      "dev.zio" %% "zio-schema"            % V.zioSchema,
      "dev.zio" %% "zio-schema-derivation" % V.zioSchema
    )
    val all: Seq[ModuleID] = Seq(zio, zioToCats, zioHttp, zioJson) ++ zioSchema
  }

  object sqlite {
    val all: Seq[ModuleID] = Seq(
      "com.typesafe.slick" %% "slick"       % "3.5.1",
      "org.slf4j"           % "slf4j-nop"   % "1.7.26",
      "org.xerial"          % "sqlite-jdbc" % "3.34.0"
    )
  }

  object telegramium {
    val all: Seq[ModuleID] = Seq(
      "io.github.apimorphism" %% "telegramium-core" % V.telegramium,
      "io.github.apimorphism" %% "telegramium-high" % V.telegramium
    )
  }

  val logback      = "ch.qos.logback"    % "logback-classic" % "1.5.6"
  val lemonlabsUri = "io.lemonlabs"     %% "scala-uri"       % "4.0.3"
  val scraper      = "net.ruippeixotog" %% "scala-scraper"   % "3.1.1"
  val javaMail     = "javax.mail"        % "mail"            % "1.4.7"

  val all: Seq[ModuleID] = scala.all ++ zio.all ++ sqlite.all ++ telegramium.all ++ Seq(logback, lemonlabsUri, scraper, javaMail)
}
