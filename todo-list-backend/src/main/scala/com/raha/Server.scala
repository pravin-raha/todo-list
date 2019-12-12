package com.raha

import cats.Monad
import cats.effect._
import cats.implicits._
import com.raha.config.{DataBaseConfig, ServerConfig}
import fs2.Stream
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpApp
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.syntax.all._
import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds

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
      client <- BlazeClientBuilder[F](global).stream
      ctx <- Stream(new Module[F](client, xa))
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(serverConfig.port, serverConfig.host)
        .withHttpApp(httpApp(ctx))
        .serve
    } yield exitCode

  def httpApp[F[_] : Async](ctx: Module[F]): HttpApp[F] = {
    Router(
      "/" -> ctx.httpEndpoint
    ).orNotFound
  }

}