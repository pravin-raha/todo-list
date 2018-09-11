package com.raha.domain.todo

import cats.effect.Async

class TodoService[F[_] : Async](todoRepository: TodoRepository[F]) {

  def create(todo: Todo): F[Int] = todoRepository.add(todo)

  def delete(id: Int): F[Int] = todoRepository.delete(id)

  def update(todo: Todo): F[Int] = todoRepository.update(todo)

  def get(id: Int): F[Option[Todo]] = todoRepository.getById(id)

  def getAll: F[List[Todo]] = todoRepository.getAll
}

object TodoService {
  def apply[F[_] : Async](todoRepository: TodoRepository[F]): TodoService[F] =
    new TodoService(todoRepository)
}
