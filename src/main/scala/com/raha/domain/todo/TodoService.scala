package com.raha.domain.todo

import cats.effect.Async

class TodoService[F[_] : Async](todoRepository: TodoRepository[F]) {

  def create(element: Element, todoId: Option[Int], userId: Int): F[Int] = todoRepository.addElement(element, todoId, userId)

  def deleteTodo(id: Int): F[Int] = todoRepository.deleteTodo(id)

  def deleteTodoElement(elementId: Int): F[Int] = todoRepository.deleteTodoElement(elementId)

  def update(element: Element): F[Int] = todoRepository.update(element)

  def get(id: Int): F[Option[Todo]] = todoRepository.getTodoById(id)

  def getAll(userId: Int): F[List[Todo]] = todoRepository.getAllTodo(userId)
}

object TodoService {
  def apply[F[_] : Async](todoRepository: TodoRepository[F]): TodoService[F] =
    new TodoService(todoRepository)
}
