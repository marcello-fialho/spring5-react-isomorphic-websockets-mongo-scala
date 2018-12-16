package com.wonderprints.isomorphic.example.services

import com.wonderprints.isomorphic.example.model.{Todo, VisibilityFilter}
import reactor.core.publisher.Mono

trait TodosService {

  def setVisibilityFilter(visibilityFilter: String): Mono[VisibilityFilter]
  def completeAllTodos(): Mono[Void]
  def clearCompleted(): Mono[Void]
  def addTodo(todo: Todo): Mono[Todo]
  def deleteTodo(id: String): Mono[Void]
  def updateTodo(id: String, todo: Todo): Mono[Todo]

}