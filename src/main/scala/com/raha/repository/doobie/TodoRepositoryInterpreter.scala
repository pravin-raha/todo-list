package com.raha.repository.doobie

import cats.effect.Async
import cats.free.Free
import com.raha.domain.todo.{Element, Todo, TodoRepository}
import doobie.free.connection
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.log.LogHandler

object TodoSql {

  def insert(element: Element, todoId: Option[Int], userId: Int): ConnectionIO[Int] =
    todoId match {
      case Some(tId) => insertElement(element, tId)
      case None => for {
        tId <- insertTodo(userId)
        eId <- insertElement(element, tId)
      } yield eId
    }

  private def insertTodo(userId: Int): ConnectionIO[Int] =
    sql"""INSERT INTO TODO (user_id) values ($userId)"""
      .updateWithLogHandler(LogHandler.jdkLogHandler)
      .withUniqueGeneratedKeys[Int]("todo_id")

  private def insertElement(element: Element, todoId: Int): ConnectionIO[Int] = {
    sql"""INSERT INTO TODOELEMENT (TODO_ID,TITLE,COMPLETED,SORT_ORDER) VALUES ($todoId, ${element.title},${element.completed},${element.sortOrder})"""
      .updateWithLogHandler(LogHandler.jdkLogHandler)
      .withUniqueGeneratedKeys[Int]("element_id")
  }

  def selectAll(userId: Int): doobie.ConnectionIO[List[(Int, Element)]] =
    sql"""
        select todo.TODO_ID, ELEMENT_ID, TITLE, COMPLETED, SORT_ORDER
        from todo
        INNER join todoelement
        on (todo.todo_id = todoelement.todo_id)
        WHERE USER_ID = $userId;
      """
      .queryWithLogHandler[(Int, Element)](LogHandler.jdkLogHandler)
      .to[List]

  def selectTodoById(todoId: Int): doobie.ConnectionIO[List[(Int, Element)]] =
    sql"""
        select TODO_ID, ELEMENT_ID, TITLE, COMPLETED, SORT_ORDER
        from todoelement
        where TODO_ID=$todoId;
      """
      .queryWithLogHandler[(Int, Element)](LogHandler.jdkLogHandler)
      .to[List]


  def deleteTodoById(todoId: Int): Free[connection.ConnectionOp, Int] = for {
    _ <- sql"delete from todo where todo_id=$todoId;".update.run
    res <- deleteTodoElementByTodoId(todoId)
  } yield res

  private def deleteTodoElementByTodoId(todoId: Int): ConnectionIO[Int] = sql"delete from todoelement where todo_id=$todoId;".update.run

  def deleteTodoElementByElementId(elementId: Int): doobie.Update0 = sql"delete from todoelement where element_id=$elementId;".update

  def deleteById(todoId: Int): doobie.Update0 = sql"delete from TODO where TODO_ID=$todoId".update

  def updateSQL(todo: Todo): doobie.Update0 = ???

  //    sql"""update TODO set
  //          title=${todo.title},
  //          completed=${todo.completed},
  //          order=${todo.order}
  //          where id=${todo.id}""".update

}

class TodoRepositoryInterpreter[F[_] : Async](xa: HikariTransactor[F]) extends TodoRepository[F] {

  import TodoSql._

  override def addElement(element: Element, todoId: Option[Int], userId: Int): F[Int] =
    insert(element, todoId, userId).transact(xa)

  override def getTodoById(todoId: Int, userId: Int): F[Option[Todo]] = selectTodoById(todoId)
    .map(rec => {
      val elements = rec.map(_._2)
      rec.headOption.map(rec => Todo(rec._1, elements))
    })
    .transact(xa)

  override def getAllTodo(userId: Int): F[List[Todo]] = selectAll(userId)
    .map(_
      .groupBy(e => e._1)
      .map(r => Todo(r._1, r._2.map(_._2)))
      .toList
    )
    .transact(xa)

  override def deleteTodo(id: Int, userId: Int): F[Int] = deleteTodoById(id).transact(xa)

  override def update(todo: Todo): F[Int] = updateSQL(todo).run.transact(xa)

  override def deleteTodoElement(todoId: Int, userId: Int, elementId: Int): F[Int] = deleteTodoElementByElementId(elementId).run.transact(xa)
}

object TodoRepositoryInterpreter {
  def apply[F[_] : Async](xa: HikariTransactor[F]): TodoRepositoryInterpreter[F] = new TodoRepositoryInterpreter(xa)
}