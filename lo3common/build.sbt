ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name             := "lo3common",
    idePackagePrefix := Some("kapibarabanka.lo3.common")
  )
  .settings(libraryDependencies ++= Dependencies.all)
  .settings(libraryDependencySchemes += "org.typelevel" %% "cats-parse" % VersionScheme.Always)
