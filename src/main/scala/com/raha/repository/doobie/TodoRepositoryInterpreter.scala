package com.raha.repository.doobie

import cats.effect.Async
import cats.free.Free
import cats.instances.list._
import cats.instances.option._
import cats.syntax.traverse._
import com.raha.domain.todo.{Element, ElementForm, Todo, TodoRepository}
import doobie.free.connection
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.log.LogHandler

import scala.language.higherKinds

object TodoSql {

  implicit val han: LogHandler = LogHandler.jdkLogHandler

  def insertTodoSql(userId: Int, element: ElementForm): ConnectionIO[Int] =
    for {
      tId <- insertTodo(userId)
      eId <- insertElement(element, tId)
    } yield eId

  private def insertTodo(userId: Int): ConnectionIO[Int] =
    sql"""INSERT INTO todo (user_id) values ($userId)"""
      .update
      .withUniqueGeneratedKeys[Int]("todo_id")

  def insertElement(element: ElementForm, todoId: Int): ConnectionIO[Int] = {
    sql"""
          INSERT INTO todoelement
          (todo_id,title,completed,sort_order)
          VALUES
          ($todoId, ${element.title},${element.completed},${element.sortOrder})
      """
      .update
      .withUniqueGeneratedKeys[Int]("element_id")
  }

  def selectAll(userId: Int): doobie.ConnectionIO[List[(Int, Option[Element])]] =
    sql"""
        select todo.todo_id, element_id, title, completed, sort_order
        from todo
        LEFT join todoelement
        on (todo.todo_id = todoelement.todo_id)
        WHERE user_id = $userId;
      """
      .query[(Int, Option[Element])]
      .to[List]

  def selectTodoById(todoId: Int): doobie.ConnectionIO[List[(Int, Element)]] =
    sql"""
        select todo_id, element_id, title, completed, sort_order
        from todoelement
        where todo_id=$todoId;
      """
      .query[(Int, Element)]
      .to[List]


  def deleteTodoById(todoId: Int): Free[connection.ConnectionOp, Int] = for {
    _ <- sql"delete from todo where todo_id=$todoId;".update.run
    res <- deleteTodoElementByTodoId(todoId)
  } yield res

  private def deleteTodoElementByTodoId(todoId: Int): ConnectionIO[Int] = sql"delete from todoelement where todo_id=$todoId;".update.run

  def deleteTodoElementByElementId(elementId: Int): doobie.Update0 = sql"delete from todoelement where element_id=$elementId;".update

  def deleteById(todoId: Int): doobie.Update0 = sql"delete from TODO where todo_id=$todoId".update

  def updateSQL(element: Element): doobie.Update0 =
    sql"""update todoelement set
            title=${element.title},
            completed=${element.completed},
            sort_order=${element.sortOrder}
            where element_id=${element.elementId}""".update

}

class TodoRepositoryInterpreter[F[_] : Async](xa: HikariTransactor[F]) extends TodoRepository[F] {

  import TodoSql._

  override def addElement(todoId: Int, elementForm: ElementForm): F[Int] =
    insertElement(elementForm, todoId = todoId).transact(xa)

  override def getTodoById(todoId: Int): F[Option[Todo]] = selectTodoById(todoId)
    .map(rec => {
      val elements = rec.map(_._2)
      rec.headOption.map(rec => Todo(rec._1, elements))
    })
    .transact(xa)

  override def getAllTodo(userId: Int): F[List[Todo]] = selectAll(userId)
    .map(_
      .groupBy(e => e._1)
      .map(r =>
        Todo(r._1,
          r._2
            .map(_._2)
            .sequence[Option, Element]
            .getOrElse(List.empty[Element])))
      .toList
    )
    .transact(xa)

  override def deleteTodo(id: Int): F[Int] = deleteTodoById(id).transact(xa)

  override def update(element: Element): F[Int] = updateSQL(element).run.transact(xa)

  override def deleteTodoElement(elementId: Int): F[Int] = deleteTodoElementByElementId(elementId).run.transact(xa)

  override def addTodo(userId: Int, elementForm: ElementForm): F[Int] = insertTodoSql(userId, elementForm).transact(xa)

}

object TodoRepositoryInterpreter {
  def apply[F[_] : Async](xa: HikariTransactor[F]): TodoRepositoryInterpreter[F] = new TodoRepositoryInterpreter(xa)
}