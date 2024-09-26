ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

scalacOptions ++= Seq(
  "-Xmax-inlines",
  "128"
)

lazy val ao3 = (project in file("ao3-scrapper"))
  .settings(name := "ao3-scrapper")
  .settings(libraryDependencies ++= Dependencies.ao3)
  .settings(libraryDependencySchemes += "org.typelevel" %% "cats-parse" % VersionScheme.Always)

lazy val root = (project in file("."))
  .dependsOn(ao3)
  .settings(name := "kapibarabot")
  .settings(libraryDependencies ++= Dependencies.kapibarabot)
  .settings(libraryDependencySchemes += "org.typelevel" %% "cats-parse" % VersionScheme.Always)
