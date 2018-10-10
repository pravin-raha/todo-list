package com.raha.domain.todo

case class Element(elementId: Option[Int], title: String, completed: Boolean = false, sortOrder: Int = 0)

case class ElementRequest(todoId: Option[Int], elementId: Option[Int], title: String, completed: Boolean = false, sortOrder: Int = 0)