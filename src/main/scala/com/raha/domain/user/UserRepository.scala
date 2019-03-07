package com.raha.domain.user

import scala.language.higherKinds

trait UserRepository[F[_]] {

  def create(user: User): F[Int]

  def get(id: String): F[Option[User]]

  def delete(id: String): F[Int]

  def put(user: User): F[Int]
}
