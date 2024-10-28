ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

scalacOptions ++= Seq(
  "-Xmax-inlines",
  "128"
)

lazy val lo3models = RootProject(file("../lo3common"))

lazy val root = (project in file("."))
  .dependsOn(lo3models)
  .settings(
    name             := "lo3api",
    idePackagePrefix := Some("kapibarabanka.lo3.api")
  )
  .settings(libraryDependencySchemes += "org.typelevel" %% "cats-parse" % VersionScheme.Always)