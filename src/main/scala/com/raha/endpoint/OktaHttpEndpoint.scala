package com.raha.endpoint

import cats.effect.Sync
import cats.syntax.all._
import com.raha.domain.okta.OktaService
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class OktaHttpEndpoint[F[_] : Sync](oktaService: OktaService[F])
  extends Http4sDsl[F] {

  val service: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "okta" =>
      Ok(oktaService.authorize)

    // OAuth2 Callback URI
    case GET -> Root / "login" / "okta" :? CodeQuery(code) :? StateQuery(state) =>
      for {
        logger <- Slf4jLogger.create[F]
        o <- Ok()
        code <- oktaService.accessToken(code, state).flatMap(oktaService.userData)
          .onError { case e => logger.error(e)(e.getMessage) }
      }
        yield
          o.withEntity(code).putHeaders(Header("Content-Type", "application/json"))
  }

  object CodeQuery extends QueryParamDecoderMatcher[String]("code")

  object StateQuery extends QueryParamDecoderMatcher[String]("state")

}

object OktaHttpEndpoint {
  def apply[F[_] : Sync](oktaService: OktaService[F]): OktaHttpEndpoint[F] =
    new OktaHttpEndpoint[F](oktaService)
}