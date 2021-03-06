import sbt._

object Dependencies {

  lazy val doobieVersion = "0.5.3"
  lazy val http4sVersion = "0.20.0-M5"
  lazy val flywayDb = "5.2.4"
  lazy val tsecV = "0.0.1-M11"

  lazy val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-specs2" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-h2" % doobieVersion,
    "org.flywaydb" % "flyway-core" % flywayDb
  )

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "io.circe" %% "circe-generic" % "0.10.1",
    "io.circe" %% "circe-literal" % "0.10.1"
  ) ++ tsec

  lazy val common = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.12.0",
    "org.typelevel" %% "cats-core" % "1.6.1",
    "mysql" % "mysql-connector-java" % "5.1.48",
    "org.typelevel" %% "cats-effect" % "1.0.0",
    "io.chrisdavenport" %% "log4cats-slf4j" % "0.3.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "io.scalaland" %% "chimney" % "0.3.2"
  )

  lazy val tsec = Seq(
    "io.github.jmcardon" %% "tsec-common" % tsecV,
    "io.github.jmcardon" %% "tsec-password" % tsecV,
    "io.github.jmcardon" %% "tsec-cipher-jca" % tsecV,
    "io.github.jmcardon" %% "tsec-cipher-bouncy" % tsecV,
    "io.github.jmcardon" %% "tsec-mac" % tsecV,
    "io.github.jmcardon" %% "tsec-signatures" % tsecV,
    "io.github.jmcardon" %% "tsec-hash-jca" % tsecV,
    "io.github.jmcardon" %% "tsec-hash-bouncy" % tsecV,
    "io.github.jmcardon" %% "tsec-libsodium" % tsecV,
    "io.github.jmcardon" %% "tsec-jwt-mac" % tsecV,
    "io.github.jmcardon" %% "tsec-jwt-sig" % tsecV,
    "io.github.jmcardon" %% "tsec-http4s" % tsecV
  )
}

