import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "todo-list",
    version := "0.1",
    scalaVersion := "2.12.6",
    scalacOptions += "-Ypartial-unification",
    libraryDependencies ++= doobie,
    libraryDependencies ++= http4s,
    libraryDependencies ++= common
  )
  .settings(
    resolvers += Resolver.sonatypeRepo("snapshots")
  )
