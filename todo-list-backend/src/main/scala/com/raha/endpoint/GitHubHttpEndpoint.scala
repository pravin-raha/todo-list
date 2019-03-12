package com.raha.endpoint

import cats.effect.Sync
import com.raha.domain.github.GitHubService
import org.http4s._
import org.http4s.dsl.Http4sDsl
import cats.syntax.all._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.language.higherKinds

class GitHubHttpEndpoint[F[_] : Sync](gitHubService: GitHubService[F])
  extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "github" =>
      Ok(gitHubService.authorize)

    // OAuth2 Callback URI
    case GET -> Root / "login" / "github" :? CodeQuery(code) :? StateQuery(state) =>
      for {
        logger <- Slf4jLogger.create[F]
        o <- Ok()
        code <- gitHubService.accessToken(code, state).flatMap(gitHubService.userData)
          .onError { case e => logger.error(e)(e.getMessage) }
      } yield o.withEntity(code).putHeaders(Header("Content-Type", "application/json"))
  }

  object CodeQuery extends QueryParamDecoderMatcher[String]("code")

  object StateQuery extends QueryParamDecoderMatcher[String]("state")

}

object GitHubHttpEndpoint {
  def apply[F[_] : Sync](gitHubService: GitHubService[F]): GitHubHttpEndpoint[F] =
    new GitHubHttpEndpoint[F](gitHubService)
}