package com.raha.domain.todo

trait TodoRepository[F[_]] {

  def addElement(element: Element, todoId: Option[Int], userId: Int): F[Int]

  def getTodoById(todoId: Int, userId: Int): F[Option[Todo]]

  def getAllTodo(userId: Int): F[List[Todo]]

  def deleteTodo(id: Int, userId: Int): F[Int]

  def deleteTodoElement(todoId: Int, userId: Int, elementId: Int): F[Int]

  def update(todo: Todo): F[Int]
}
