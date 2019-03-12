package com.raha.domain.user

sealed abstract class ResponseError(status: Int, massage: String)

case class UserNotFoundError(userName: String)
    extends ResponseError(404, s"$userName not Found")

case class UserAlreadyExist(userName: String)
    extends ResponseError(409, s"$userName already exist")
