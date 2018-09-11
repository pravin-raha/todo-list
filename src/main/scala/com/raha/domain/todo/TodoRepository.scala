package com.raha.domain.todo

trait TodoRepository[F[_]] {

  def add(todo: Todo): F[Int]

  def getById(id: Int): F[Option[Todo]]

  def getAll: F[List[Todo]]

  def delete(id: Int): F[Int]

  def update(todo: Todo): F[Int]
}
