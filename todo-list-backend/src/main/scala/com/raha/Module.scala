package com.raha

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.raha.domain.todo.TodoService
import com.raha.domain.user.UserService
import com.raha.endpoint.{GitHubHttpEndpoint, OktaHttpEndpoint, TodoEndpoint, UserEndpoint}
import com.raha.repository.doobie.{TodoRepositoryInterpreter, UserRepositoryInterpreter}
import doobie.hikari.HikariTransactor
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import org.http4s.client.Client
import com.raha.domain.github.GitHubService
import cats.syntax.semigroupk._
import com.raha.domain.okta.OktaService

import scala.language.higherKinds

class Module[F[_]](client: Client[F], xa: HikariTransactor[F])(
  implicit F: ConcurrentEffect[F],
  CS: ContextShift[F],
  T: Timer[F]) {

  implicit def unsafeLogger: SelfAwareStructuredLogger[F] = Slf4jLogger.unsafeCreate[F]

  private lazy val userRepoInterpreter: UserRepositoryInterpreter[F] = UserRepositoryInterpreter(xa = xa)
  private lazy val userService: UserService[F] = UserService(userRepoInterpreter)
  private lazy val userEndpoint: HttpRoutes[F] = UserEndpoint[F](userService = userService).service

  private lazy val todoRepoInterpreter: TodoRepositoryInterpreter[F] = TodoRepositoryInterpreter(xa = xa)
  private lazy val todoService: TodoService[F] = TodoService(todoRepository = todoRepoInterpreter)
  private lazy val todoEndpoint = TodoEndpoint[F](todoService).service
  private lazy val gitHubService = GitHubService(client)
  private lazy val gitHubHttpEndpoint = GitHubHttpEndpoint(gitHubService).service

  private lazy val oktaService = OktaService(client)
  private lazy val oktaHttpEndpoint = OktaHttpEndpoint(oktaService).service

  val httpEndpoint: HttpRoutes[F] = userEndpoint <+> todoEndpoint <+> oktaHttpEndpoint

}