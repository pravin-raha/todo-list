package com.raha.domain.todo

import scala.language.higherKinds

trait TodoRepository[F[_]] {

  def addElement(todoId: Int, elementForm: ElementForm): F[Int]

  def addTodo(userId: Int, elementForm: ElementForm): F[Int]

  def getTodoById(todoId: Int): F[Option[Todo]]

  def getAllTodo(userId: Int): F[List[Todo]]

  def deleteTodo(id: Int): F[Int]

  def deleteTodoElement(elementId: Int): F[Int]

  def update(element: Element): F[Int]
}
