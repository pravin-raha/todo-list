package com.raha.endpoint

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.raha.domain.github.GitHubService
import org.http4s._
import org.http4s.dsl.Http4sDsl

class GitHubHttpEndpoint[F[_]](gitHubService: GitHubService[F])(implicit F: Sync[F])
  extends Http4sDsl[F] {

  object CodeQuery extends QueryParamDecoderMatcher[String]("code")
  object StateQuery extends QueryParamDecoderMatcher[String]("state")

  val service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "github" =>
      Ok(gitHubService.authorize)

    // OAuth2 Callback URI
    case GET -> Root / "login" / "github" :? CodeQuery(code) :? StateQuery(state) =>
      for {
        o <- Ok()
        code <- gitHubService.accessToken(code, state).flatMap(gitHubService.userData)
      } yield o.withEntity(code).putHeaders(Header("Content-Type", "application/json"))
  }

}