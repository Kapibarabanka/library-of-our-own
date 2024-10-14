ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name             := "lo3models",
    idePackagePrefix := Some("kapibarabanka.lo3.models")
  )
