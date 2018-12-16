package com.wonderprints.isomorphic.example.services

import java.util
import java.util.function.{BiFunction, Function}
import com.wonderprints.isomorphic.example.model.{Todo, VisibilityFilter}
import com.wonderprints.isomorphic.example.repositories.{TodoRepository, VisibilityFilterRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.{Flux, Mono}
import scala.collection.JavaConverters._

@Service("todosService")
@Autowired
class TodosServiceImpl(private val visibilityFilterRepository: VisibilityFilterRepository,
                       private val todoRepository: TodoRepository)  extends TodosService {

  private val reducer: BiFunction[util.ArrayList[Todo], Todo, util.ArrayList[Todo]] =
    (acc: util.ArrayList[Todo], curr: Todo) => {
      acc.add(curr)
      acc
    }

  private val findAndProcessAll: BiFunction[TodoRepository, Function[util.List[Todo], Mono[Void]], Mono[Void]] = (repository, func) =>
    repository.findAll().reduce(new util.ArrayList[Todo](), reducer).flatMap(func).then()

  override def setVisibilityFilter(visibilityFilter: String): Mono[VisibilityFilter] =
    synchronized {
      visibilityFilterRepository
        .deleteAll()
        .flatMap(_ => visibilityFilterRepository.save(VisibilityFilter(visibilityFilter)))
    }

  override def completeAllTodos(): Mono[Void] = synchronized {
    val completeAll: Function[util.List[Todo], Mono[Void]] =
      (todosList: util.List[Todo]) => {
        val areAllMarked: Boolean = todosList.asScala.count(todo => todo.isCompleted) == todosList.asScala.size
        val newTodosList: util.List[Todo] = todosList.asScala.map(todo => Todo(todo.getId, todo.getText, !areAllMarked)).map(todo => Todo(todo)).asJava
        todoRepository
          .deleteAll()
          .thenMany(
            Flux
              .fromStream(newTodosList.stream())
              .flatMap(todo => todoRepository.save(todo)))
          .then()
      }
    try findAndProcessAll.apply(todoRepository, completeAll)
    catch {
      case _: Exception => Mono.empty()

    }
  }

  override def clearCompleted(): Mono[Void] = synchronized {
    val clearCompleted: Function[util.List[Todo], Mono[Void]] =
      (todosList: util.List[Todo]) => {
        val newTodosList: util.List[Todo] = todosList.asScala.filter(todo => !todo.isCompleted).map(todo => Todo(todo)).asJava
        todoRepository
          .deleteAll()
          .thenMany(
            Flux
              .fromStream(newTodosList.stream())
              .flatMap(todo => todoRepository.save(todo)))
          .then()
      }
    try findAndProcessAll.apply(todoRepository, clearCompleted)
    catch {
      case _: Exception => Mono.empty()
    }
  }

  override def addTodo(todo: Todo): Mono[Todo] = synchronized {
    todoRepository.save(todo)
  }

  override def deleteTodo(id: String): Mono[Void] = synchronized {
    todoRepository
      .findById(id)
      .flatMap((t: Todo) => todoRepository.delete(t))
  }

  override def updateTodo(id: String, todo: Todo): Mono[Todo] = synchronized {
    todoRepository.save(todo)
  }

}
