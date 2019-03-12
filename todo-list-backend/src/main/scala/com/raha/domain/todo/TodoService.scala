package com.raha.domain.todo

import cats.effect.Async

import scala.language.higherKinds

class TodoService[F[_] : Async](todoRepository: TodoRepository[F]) {

  def createElement(todoId: Int, elementForm: ElementForm): F[Int] = todoRepository.addElement(todoId, elementForm)

  def createTodo(userId: Int, elementForm: ElementForm): F[Int] = todoRepository.addTodo(userId, elementForm)

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
