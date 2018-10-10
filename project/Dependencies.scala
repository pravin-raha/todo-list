import sbt._

object Dependencies {
  lazy val doobieVersion = "0.5.3"
  lazy val http4sVersion = "1.0.0-SNAPSHOT"
  lazy val flywayDb = "5.1.4"

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-specs2" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.flywaydb" % "flyway-core" % flywayDb
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "io.circe" %% "circe-generic" % "0.10.0-M1",
    "io.circe" %% "circe-literal" % "0.10.0-M1"
  )

  val common = Seq(
    "mysql" % "mysql-connector-java" % "5.1.24",
    "org.typelevel" %% "cats-core" % "1.2.0",
    "org.typelevel" %% "cats-effect" % "1.0.0-RC2",
    "com.github.pureconfig" %% "pureconfig" % "0.9.2",
    "io.chrisdavenport" %% "log4cats-slf4j" % "0.1.1",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "io.scalaland" %% "chimney" % "0.2.1"
  )

}

