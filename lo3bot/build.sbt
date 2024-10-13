ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

scalacOptions ++= Seq(
  "-Xmax-inlines",
  "128"
)

lazy val root = (project in file("."))
  .settings(
    name             := "lo3bot",
    idePackagePrefix := Some("kapibarabanka.lo3.bot")
  )
  .settings(libraryDependencies ++= Dependencies.all)
  .settings(libraryDependencySchemes += "org.typelevel" %% "cats-parse" % VersionScheme.Always)
