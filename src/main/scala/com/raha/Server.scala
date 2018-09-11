package com.raha

import cats.effect.IO
import com.raha.config.{DataBaseConfig, ServerConfig}
import com.raha.domain.todo.TodoService
import com.raha.domain.user.UserService
import com.raha.repository.doobie.{TodoRepositoryInterpreter, UserRepositoryInterpreter}
import com.raha.service.{TodoEndpoint, UserEndpoint}
import fs2.{Stream, StreamApp}
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeBuilder
import pureconfig.loadConfigOrThrow

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] {

  implicit def unsafeLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.unsafeCreate[IO]

  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = {
    for {
      dataBaseConfig <- Stream.eval(IO(loadConfigOrThrow[DataBaseConfig]("database")))
      serverConfig <- Stream.eval(IO(loadConfigOrThrow[ServerConfig]("server")))
      xa <- Stream.eval(DataBaseConfig.dbTransactor[IO](dataBaseConfig))
      userRepoInterpreter = UserRepositoryInterpreter(xa = xa)
      userService = UserService(userRepoInterpreter)
      todoRepoInterpreter = TodoRepositoryInterpreter(xa = xa)
      todoService = TodoService(todoRepository = todoRepoInterpreter)
      exitCode <- BlazeBuilder[IO]
        .bindHttp(serverConfig.port, serverConfig.host)
        .mountService(UserEndpoint[IO](userService = userService).service, "/")
        .mountService(TodoEndpoint[IO](todoService).service, "/")
        .serve
    } yield exitCode

  }
}