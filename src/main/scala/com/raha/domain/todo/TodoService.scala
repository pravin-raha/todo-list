package com.raha.domain.todo

import cats.effect.Async

class TodoService[F[_] : Async](todoRepository: TodoRepository[F]) {

  def create(element: Element, todoId: Option[Int], userId: Int): F[Int] = todoRepository.addElement(element, todoId, userId)

  def delete(id: Int): F[Int] = todoRepository.delete(id)

  def update(todo: Todo): F[Int] = todoRepository.update(todo)

  def get(id: Int, userId: Int): F[Option[Todo]] = todoRepository.getById(id, userId)

  def getAll(userId: Int): F[List[Todo]] = todoRepository.getAll(userId)
}

object TodoService {
  def apply[F[_] : Async](todoRepository: TodoRepository[F]): TodoService[F] =
    new TodoService(todoRepository)
}
