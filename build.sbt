import Dependencies._

ThisBuild / organization := "com.raha"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / scalacOptions += "-Ypartial-unification"

lazy val root = (project in file("."))
  .aggregate(todoListBackend)

lazy val todoListBackend = (project in file("todo-list-backend"))
  .settings(
    name := "todo-list-backend",
    libraryDependencies ++= doobie,
    libraryDependencies ++= http4s,
    libraryDependencies ++= common,
    flywayUrl := "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    flywayUser := "sa",
    flywayLocations += "db/migration"
  )
  .settings(
    resolvers += Resolver.sonatypeRepo("snapshots")
  ).enablePlugins(FlywayPlugin)
