package com.wonderprints.isomorphic.example.actions

import java.util.Objects
import com.wonderprints.isomorphic.example.model.Todo
import scala.beans.BeanProperty

sealed abstract class Action {

  @BeanProperty
  var `type`: String = _

  def this(`type`: String) = {
    this()
    this.`type` = `type`
  }

  override def equals(o: Any): Boolean = {
    if (this == o) return true
    if (!o.isInstanceOf[Action]) return false
    val action: Action = o.asInstanceOf[Action]
    Objects.equals(getType, action.getType)
  }

  override def hashCode(): Int = Objects.hash(getType)

}

case class AddTodo() extends Action("ADD_TODO") with Equals {

  @BeanProperty var id: String = _
  @BeanProperty var text: String = _

  def this(id: String, text: String) = {
    this()
    this.id = id
    this.text = text
  }
}

case class DeleteTodo() extends Action("DELETE_TODO") with Equals {

  @BeanProperty var id: String = _

  def this(id: String) = {
    this()
    this.id = id
  }
}

case class SetVisibilityFilter() extends Action("SET_VISIBILITY_FILTER") with Equals {

  @BeanProperty var filter: String = _

  def this(filter: String) = {
    this()
    this.filter = filter
  }
}

case class UpdateTodo() extends Action("UPDATE_TODO") with Equals {

  @BeanProperty var todo: Todo = _

  def this(todo: Todo) = {
    this()
    this.todo = todo
  }
}