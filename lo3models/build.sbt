ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name                                := "lo3models",
    idePackagePrefix                    := Some("kapibarabanka.lo3.models"),
    libraryDependencies += "dev.zio"    %% "zio-schema"            % "1.5.0",
    libraryDependencies += "dev.zio"    %% "zio-schema-derivation" % "1.5.0",
    libraryDependencies += "dev.zio"    %% "zio-http"              % "3.0.1",
    libraryDependencies += "org.scalaz" %% "scalaz-core"           % "7.3.8"
  )
