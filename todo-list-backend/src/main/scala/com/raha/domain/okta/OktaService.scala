package com.raha.domain.okta

import cats.effect.Sync
import cats.syntax.functor._
import com.raha.config.OidcConfig
import fs2.Stream
import io.circe.generic.auto._
import org.http4s.circe.{jsonOf, _}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.FollowRedirect
import org.http4s.{Header, Headers, Request, Uri}
import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._

import scala.language.higherKinds

class OktaService[F[_] : Sync](client: Client[F]) extends Http4sClientDsl[F] {
  private lazy val oidcConfig = loadConfigOrThrow[OidcConfig]("oidc")
  private lazy val ClientId = oidcConfig.cliendId
  private lazy val ClientSecret = oidcConfig.clientSecret
  private lazy val RedirectUri = oidcConfig.redirectUrl
  private lazy val redirect: Client[F] = FollowRedirect(1)(client)

  val authorize: Stream[F, Byte] = {
    val uri = Uri
      .uri("https://dev-501761.okta.com")
      .withPath("/oauth2/default/v1/authorize")
      .withQueryParam("client_id", ClientId)
      .withQueryParam("redirect_uri", RedirectUri)
      .withQueryParam("scope", "openid profile")
      .withQueryParam("state", "test_api")
      .withQueryParam("response_type", "code")

    redirect.stream(Request[F](uri = uri)).flatMap(_.body)
  }

  import org.http4s.dsl.io._

  def accessToken(code: String, state: String): F[String] = {
    val uri = Uri
      .uri("https://dev-501761.okta.com")
      .withPath("/oauth2/default/v1/token")
      .withQueryParam("grant_type", "authorization_code")
      .withQueryParam("redirect_uri", RedirectUri)
      .withQueryParam("code", code)
      .withQueryParam("client_id", ClientId)
      .withQueryParam("client_secret", ClientSecret)
      .withQueryParam("scope", "openid profile")

    val headers: Headers = Headers(
      Header("accept", "application/json"),
      Header("content-type", "application/x-www-form-urlencoded")
    )

    val req = POST(uri).map(_.withHeaders(headers))

    client
      .expect[AccessTokenResponse](req)(jsonOf[F, AccessTokenResponse])
      .map(r => r.access_token)
  }

  def userData(accessToken: String): F[String] = {
    val request = Request[F](uri = Uri.uri("https://dev-501761.okta.com/oauth2/default/v1/userinfo"))
      .putHeaders(
        Header("Authorization", s"Bearer $accessToken"),
        Header("accept", "application/json")
      )

    client.expect[String](request)
  }

  private case class AccessTokenResponse(access_token: String)

}

object OktaService {
  def apply[F[_] : Sync](client: Client[F]): OktaService[F] = new OktaService[F](client)
}