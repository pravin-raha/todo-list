package com.raha.domain.todo

case class Todo(id: String, title: String, completed: Boolean = false, order: Int = 0)