import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "todo-list",
    version := "0.1",
    scalaVersion := "2.12.6",
    scalacOptions += "-Ypartial-unification",
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


