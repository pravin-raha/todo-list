package com.raha.service

import cats.effect.Async
import cats.syntax.all._
import com.raha.domain.todo.{Todo, TodoService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, _}


class TodoEndpoint[F[_] : Async](todoService: TodoService[F]) extends Http4sDsl[F] {

  implicit val decoder: EntityDecoder[F, Todo] = jsonOf[F, Todo]

  def service: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "todo" / IntVar(id) => todoService.get(id).flatMap {
      case Some(todo) => Ok(todo.asJson)
      case None => NotFound("Todo Item not found")
    }.handleErrorWith(e => ServiceUnavailable())

    case GET -> Root / "todo" => todoService.getAll.flatMap(t => Ok(t.asJson))

    case req@POST -> Root / "todo" => for {
      todo <- req.as[Todo]
      res <- todoService.create(todo = todo)
        .flatMap(e => Ok(todo.asJson))
        .handleErrorWith(e => ServiceUnavailable())
    } yield res

    case DELETE -> Root / "todo" / IntVar(id) => todoService.delete(id).flatMap(_ => Ok("todo deleted"))

    case req@PUT -> Root / "todo" => for {
      todo <- req.as[Todo]
      res <- todoService.update(todo).flatMap(_ => Ok(todo.asJson))
    } yield res
  }
}

object TodoEndpoint {
  def apply[F[_] : Async](todoService: TodoService[F]): TodoEndpoint[F] = new TodoEndpoint(todoService)
}