package com.raha.domain.github

import cats.effect.Sync
import cats.syntax.functor._
import com.raha.config.OidcConfig
import fs2.Stream
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{Header, Request, Uri}
import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._

import scala.language.higherKinds

class GitHubService[F[_] : Sync](client: Client[F]) extends Http4sClientDsl[F] {

  private lazy val oidcConfig = loadConfigOrThrow[OidcConfig]("oidc")
  private lazy val ClientId = oidcConfig.cliendId
  private lazy val ClientSecret = oidcConfig.clientSecret
  private lazy val RedirectUri = oidcConfig.redirectUrl

  val authorize: Stream[F, Byte] = {
    val uri = Uri
      .uri("https://github.com")
      .withPath("/login/oauth/authorize")
      .withQueryParam("client_id", ClientId)
      .withQueryParam("redirect_uri", RedirectUri)
      .withQueryParam("scopes", "public_repo")
      .withQueryParam("state", "test_api")

    client.stream(Request[F](uri = uri)).flatMap(_.body)
  }

  def accessToken(code: String, state: String): F[String] = {
    val uri = Uri
      .uri("https://github.com")
      .withPath("/login/oauth/access_token")
      .withQueryParam("client_id", ClientId)
      .withQueryParam("client_secret", ClientSecret)
      .withQueryParam("code", code)
      .withQueryParam("redirect_uri", RedirectUri)
      .withQueryParam("state", state)

    client
      .expect[AccessTokenResponse](Request[F](uri = uri))(jsonOf[F, AccessTokenResponse])
      .map(_.access_token)
  }

  def userData(accessToken: String): F[String] = {
    val request = Request[F](uri = Uri.uri("https://api.github.com/user"))
      .putHeaders(Header("Authorization", s"token $accessToken"))

    client.expect[String](request)
  }

  private case class AccessTokenResponse(access_token: String)

}

object GitHubService {
  def apply[F[_] : Sync](client: Client[F]): GitHubService[F] = new GitHubService[F](client)
}