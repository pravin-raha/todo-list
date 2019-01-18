package com.raha

import cats.Monad
import cats.effect._
import cats.implicits._
import com.raha.config.{DataBaseConfig, ServerConfig}
import com.raha.domain.todo.TodoService
import com.raha.domain.user.UserService
import com.raha.repository.doobie.{TodoRepositoryInterpreter, UserRepositoryInterpreter}
import com.raha.service.{TodoEndpoint, UserEndpoint}
import doobie.hikari.HikariTransactor
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.syntax.all._
import pureconfig.loadConfigOrThrow

import scala.language.higherKinds
import scala.concurrent.ExecutionContext.Implicits.global

object Server extends IOApp {

  implicit def unsafeLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.unsafeCreate[IO]

  override def run(args: List[String]): IO[ExitCode] = HttpServer.stream[IO].compile.drain.as(ExitCode.Success)
}

object HttpServer {

  def stream[F[_] : ConcurrentEffect : ContextShift : Timer]: Stream[F, ExitCode] =
    for {
      dataBaseConfig <- Stream.eval(Monad[F].pure(loadConfigOrThrow[DataBaseConfig]("database")))
      serverConfig <- Stream.eval(Monad[F].pure(loadConfigOrThrow[ServerConfig]("server")))
      xa <- Stream.eval(DataBaseConfig.dbTransactor[F](dataBaseConfig))
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(serverConfig.port, serverConfig.host)
        .withHttpApp(httpApp(xa))
        .serve
    } yield exitCode

  def httpApp[F[_] : Async](xa: HikariTransactor[F]): HttpApp[F] = {
    implicit def unsafeLogger: SelfAwareStructuredLogger[F] = Slf4jLogger.unsafeCreate[F]
    val userRepoInterpreter = UserRepositoryInterpreter(xa = xa)
    val userService = UserService(userRepoInterpreter)
    val todoRepoInterpreter = TodoRepositoryInterpreter(xa = xa)
    val todoService = TodoService(todoRepository = todoRepoInterpreter)
    Router(
      "/" -> UserEndpoint[F](userService = userService).service,
      "/" -> TodoEndpoint[F](todoService).service
    ).orNotFound
  }

}