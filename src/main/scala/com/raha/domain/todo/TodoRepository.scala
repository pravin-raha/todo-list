package com.raha.domain.todo

trait TodoRepository[F[_]] {

  def addElement(element: Element, todoId: Option[Int], userId: Int): F[Int]

  def getById(todoId: Int, userId: Int): F[Option[Todo]]

  def getAll(userId: Int): F[List[Todo]]

  def delete(id: Int): F[Int]

  def update(todo: Todo): F[Int]
}
