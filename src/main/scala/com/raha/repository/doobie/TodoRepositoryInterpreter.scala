package com.raha.repository.doobie

import cats.effect.Async
import com.raha.domain.todo.{Todo, TodoRepository}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.log.LogHandler

object TodoSql {

  def insert(todo: Todo): doobie.Update0 =
    sql"""insert into TODO (id, title, completed, `order`) values (${todo.id},${todo.title},${todo.completed},${todo.order})""".updateWithLogHandler(LogHandler.jdkLogHandler)

  def selectAll(): doobie.ConnectionIO[List[Todo]] = sql"select * from TODO".query[Todo].to[List]

  def selectById(id: Int): doobie.Query0[Todo] = sql"select * from TODO where id=$id".queryWithLogHandler[Todo](LogHandler.jdkLogHandler)

  def deleteById(id: Int): doobie.Update0 = sql"delete from TODO where id=$id".update

  def updateSQL(todo: Todo): doobie.Update0 =
    sql"""update TODO set
          title=${todo.title},
          completed=${todo.completed},
          order=${todo.order}
          where id=${todo.id}""".update

}

class TodoRepositoryInterpreter[F[_] : Async](xa: HikariTransactor[F]) extends TodoRepository[F] {

  import TodoSql._

  override def add(todo: Todo): F[Int] = insert(todo).run.transact(xa)

  override def getById(id: Int): F[Option[Todo]] = selectById(id).option.transact(xa)

  override def getAll: F[List[Todo]] = selectAll().transact(xa)

  override def delete(id: Int): F[Int] = deleteById(id).run.transact(xa)

  override def update(todo: Todo): F[Int] = updateSQL(todo).run.transact(xa)
}

object TodoRepositoryInterpreter {
  def apply[F[_] : Async](xa: HikariTransactor[F]): TodoRepositoryInterpreter[F] = new TodoRepositoryInterpreter(xa)
}