package com.raha

import cats.effect.IO
import com.raha.config.{DataBaseConfig, ServerConfig}
import com.raha.domain.user.UserService
import com.raha.repository.doobie.UserRepositoryInterpreter
import com.raha.service.UserEndpoint
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder
import pureconfig.loadConfigOrThrow

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = {
    for {
      dataBaseConfig <- Stream.eval(IO(loadConfigOrThrow[DataBaseConfig]("database")))
      serverConfig <- Stream.eval(IO(loadConfigOrThrow[ServerConfig]("server")))
      xa <- Stream.eval(DataBaseConfig.dbTransactor[IO](dataBaseConfig))
      userRepoInterpreter = UserRepositoryInterpreter(xa = xa)
      userService = UserService(userRepoInterpreter)
      exitCode <- BlazeBuilder[IO]
        .bindHttp(serverConfig.port, serverConfig.host)
        .mountService(UserEndpoint[IO](userService = userService).service, "/")
        .serve
    } yield exitCode

  }
}