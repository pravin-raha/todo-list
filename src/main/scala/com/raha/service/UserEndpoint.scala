package com.raha.service

import _root_.io.chrisdavenport.log4cats._
import _root_.io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.effect.Async
import cats.syntax.all._
import com.raha.domain.user.{User, UserService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

class UserEndpoint[F[_] : Async](userService: UserService[F]) extends Http4sDsl[F] {

  implicit def unsafeLogger: SelfAwareStructuredLogger[F] = Slf4jLogger.unsafeCreate[F]

  implicit val decoder: EntityDecoder[F, User] = jsonOf[F, User]

  def service: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "user" =>
      for {
        user <- req.as[User]
        res <- userService.createUser(user).attempt.flatMap {
          case Left(error) => for {
            res <- Ok(error.getMessage)
          } yield res
          case Right(_) => Ok(user.asJson)
        }
      } yield res

    case GET -> Root / "user" / id =>
      for {
        user <- userService.getUser(id).attempt.flatMap {
          case Left(error) => for {
            res <- NotFound(error.getMessage)
          } yield res
          case Right(u) => Ok(u.asJson)
        }
      } yield user
  }

}

object UserEndpoint {
  def apply[F[_] : Async](userService: UserService[F]): UserEndpoint[F] = new UserEndpoint[F](userService)
}
