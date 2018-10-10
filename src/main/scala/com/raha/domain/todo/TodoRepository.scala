package com.raha.domain.todo

trait TodoRepository[F[_]] {

  def addElement(element: Element, todoId: Option[Int], userId: Int): F[Int]

  def getTodoById(todoId: Int): F[Option[Todo]]

  def getAllTodo(userId: Int): F[List[Todo]]

  def deleteTodo(id: Int): F[Int]

  def deleteTodoElement(elementId: Int): F[Int]

  def update(element: Element): F[Int]
}
