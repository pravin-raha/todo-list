package com.raha.service

import cats.effect.Async
import cats.syntax.all._
import com.raha.domain.todo.{Element, ElementRequest, Todo, TodoService}
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, _}


class TodoEndpoint[F[_] : Async](todoService: TodoService[F]) extends Http4sDsl[F] {

  implicit val elementRequestDecoder: EntityDecoder[F, ElementRequest] = jsonOf[F, ElementRequest]
  implicit val elementDecoder: EntityDecoder[F, Element] = jsonOf[F, Element]
  implicit val decoder: EntityDecoder[F, Todo] = jsonOf[F, Todo]

  def service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "todo" / IntVar(id) => todoService.get(id).flatMap {
      case Some(todo) => Ok(todo.asJson)
      case None => NotFound("Todo Item not found")
    }.handleErrorWith(e => ServiceUnavailable())

    case GET -> Root / "todo" => todoService.getAll(101).flatMap(t => Ok(t.asJson))

    case req@POST -> Root / "todo" => for {
      elementRequest <- req.as[ElementRequest]
      res <- todoService.create(elementRequest.into[Element].transform, elementRequest.todoId, 101)
        .flatMap(e => Ok())
        .handleErrorWith(e => ServiceUnavailable())
    } yield res

    case DELETE -> Root / "todo" / IntVar(todoId) => todoService.deleteTodo(todoId).flatMap(_ => Ok("todo deleted"))

    case DELETE -> Root / "todo" / IntVar(todoId) / IntVar(elementId) =>
      todoService.deleteTodoElement(elementId).flatMap(_ => Ok("element deleted"))

    case req@PUT -> Root / "todo" / IntVar(todoId) => for {
      element <- req.as[Element]
      res <- todoService.update(element).flatMap(_ => Ok(element.asJson))
    } yield res

  }
}

object TodoEndpoint {
  def apply[F[_] : Async](todoService: TodoService[F]): TodoEndpoint[F] = new TodoEndpoint(todoService)
}