package com.raha.endpoint

import _root_.io.chrisdavenport.log4cats._
import cats.effect.Async
import cats.syntax.all._
import com.raha.domain.user.{User, UserService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

import scala.language.higherKinds

class UserEndpoint[F[_] : Async](userService: UserService[F])(implicit unsafeLogger: SelfAwareStructuredLogger[F]) extends Http4sDsl[F] {

  implicit val decoder: EntityDecoder[F, User] = jsonOf[F, User]

  def service: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "user" =>
      for {
        user <- req.as[User]
        res <- userService.createUser(user)
          .flatMap(_ => Ok(user.asJson))
          .handleErrorWith(error => ServiceUnavailable(error.getMessage))
      } yield res

    case GET -> Root / "user" / id => userService.getUser(id).flatMap {
      case None => NotFound("User not found")
      case Some(user) => Ok(user.asJson)
    }.handleErrorWith(error => ServiceUnavailable(error.getMessage))

  }
}

object UserEndpoint {
  def apply[F[_] : Async](userService: UserService[F])(implicit unsafeLogger: SelfAwareStructuredLogger[F]): UserEndpoint[F] = new UserEndpoint[F](userService)
}
