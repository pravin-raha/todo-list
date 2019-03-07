package com.raha.repository.doobie

import cats.Monad
import cats.effect.Async
import com.raha.domain.user.{User, UserRepository}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.update.Update0

import scala.language.higherKinds

private object UserSql {
  def insert(user: User): Update0 =
    sql"insert into USER (id, name, email) values (${user.id.get}, ${user.name}, ${user.email})".updateWithLogHandler(LogHandler.jdkLogHandler)

  def select(id: String): doobie.Query0[User] = sql"""
    SELECT *
    FROM USER
    WHERE ID = $id
  """.queryWithLogHandler[User](LogHandler.jdkLogHandler)


  def delete(id: String): doobie.Update0 = sql"delete from USER where id = $id".updateWithLogHandler(LogHandler.jdkLogHandler)

  def update(user: User): doobie.Update0 = sql"update USER set id = ${user.id}, name = ${user.name}, email = ${user.email} where id = ${user.id}".updateWithLogHandler(LogHandler.jdkLogHandler)
}

class UserRepositoryInterpreter[F[_] : Async](xa: HikariTransactor[F])
  extends UserRepository[F] {

  import UserSql._

  override def create(user: User): F[Int] = insert(user).run.transact(xa)

  override def get(id: String): F[Option[User]] = select(id).option.transact(xa)

  override def delete(id: String): F[Int] = UserSql.delete(id).run.transact(xa)

  override def put(user: User): F[Int] = UserSql.update(user).run.transact(xa)

}

object UserRepositoryInterpreter {
  def apply[F[_] : Monad : Async](
                                   xa: HikariTransactor[F]): UserRepositoryInterpreter[F] =
    new UserRepositoryInterpreter[F](xa)
}
