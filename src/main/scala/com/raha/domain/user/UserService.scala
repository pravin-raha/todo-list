package com.raha.domain.user

import cats.effect.Async

class UserService[F[_] : Async](userRepository: UserRepository[F]) {

  def createUser(user: User): F[Int] =
    userRepository.create(user)

  def getUser(id: String): F[Option[User]] = userRepository.get(id)

  def deleteUser(id: String): F[Int] =
    userRepository.delete(id)

  def updateUser(user: User): F[Int] =
    userRepository.put(user)

}

object UserService {
  def apply[F[_] : Async](userRepository: UserRepository[F]): UserService[F] =
    new UserService(userRepository)
}
