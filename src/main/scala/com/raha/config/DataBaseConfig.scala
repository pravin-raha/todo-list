package com.raha.config

import cats._
import cats.effect.Async
import cats.implicits._
import doobie.hikari._

case class DataBaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    poolName: String,
    poolSize: Int
)

object DataBaseConfig {

  def dbTransactor[F[_]: Async: Monad](
      dataBaseConfig: DataBaseConfig): F[HikariTransactor[F]] =
    for {
      xa <- HikariTransactor.newHikariTransactor[F](
        dataBaseConfig.driver,
        dataBaseConfig.url,
        dataBaseConfig.user,
        dataBaseConfig.password
      )
      _ <- configure(dataBaseConfig.poolName, dataBaseConfig.poolSize, xa)
    } yield xa

  private def configure[F[_]: Monad](
      poolName: String,
      poolSize: Int,
      xa: HikariTransactor[F])(implicit monad: Monad[F]): F[Unit] = {
    xa.configure { hx =>
      hx.setPoolName(poolName)
      hx.setMaximumPoolSize(poolSize) // (core * 2) + 1
      monad.unit
    }
  }
}
