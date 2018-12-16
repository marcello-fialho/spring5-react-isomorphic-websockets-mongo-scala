package com.wonderprints.isomorphic.example.services

import java.io.IOException
import java.util.regex.Pattern

import com.fasterxml.jackson.databind.ObjectMapper
import com.wonderprints.isomorphic.example.actions._
import com.wonderprints.isomorphic.example.model.Todo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service("clientMessageDecoder")
@Autowired
class ClientMessageDecoder(private val todosService: TodosService) {

  private val pattern: Pattern =
    Pattern.compile(".*type[^:]*:[^A-Z]*([A-Z_][A-Z_]*).*")

  def handleMessage(message: String): Mono[String] = {
    def toMonoStr[T] = (mono: Mono[T]) => mono.then(Mono.just(message))
    val objectMapper = new ObjectMapper()
    val matcher = pattern.matcher(message)
    if (matcher.matches()) {
      val actionType: String = matcher.group(1)
      try actionType match {
        case "ADD_TODO" =>
          val addTodoAction: AddTodo =
            objectMapper.readValue(message, classOf[AddTodo])
          toMonoStr(todosService
            .addTodo(Todo(addTodoAction.getId, addTodoAction.getText, completed = false)))
        case "DELETE_TODO" =>
          val deleteTodoAction: DeleteTodo =
            objectMapper.readValue(message, classOf[DeleteTodo])
          toMonoStr(todosService
            .deleteTodo(deleteTodoAction.getId))
        case "UPDATE_TODO" =>
          val updateTodoAction: UpdateTodo =
            objectMapper.readValue(message, classOf[UpdateTodo])
          toMonoStr(todosService
            .updateTodo(
              updateTodoAction.getTodo.getId,
              updateTodoAction.getTodo))
        case "COMPLETE_ALL_TODOS" =>
          toMonoStr(todosService.completeAllTodos())
        case "CLEAR_COMPLETED" =>
          toMonoStr(todosService.clearCompleted())
        case "SET_VISIBILITY_FILTER" =>
          val setVisibilityFilterAction: SetVisibilityFilter =
            objectMapper.readValue(message, classOf[SetVisibilityFilter])
          toMonoStr(todosService
            .setVisibilityFilter(setVisibilityFilterAction.getFilter))
      } catch {
        case _: IOException => {
          println("ClientMessageDecoder: not an action message")
          Mono.empty()
        }
      }
    } else {
      println("ClientMessageDecoder: not an action message")
      Mono.empty()
    }
  }

}
