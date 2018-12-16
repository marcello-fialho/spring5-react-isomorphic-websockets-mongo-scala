package com.wonderprints.isomorphic.example.model

import scala.beans.{BeanProperty, BooleanBeanProperty}
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

object Todo {
  def apply(id: String, text: String, completed: Boolean) = new Todo(id, text, completed)
  def apply(original: Todo): Todo =
    new Todo(original.id, original.getText, original.isCompleted)
}

@Document
class Todo extends Equals {
  @Id @BeanProperty var id: String = _
  @BeanProperty var text: String = _
  @BooleanBeanProperty var completed: Boolean = _

  def this(_id: String, _text: String, _completed: Boolean) = {
    this()
    this.id = _id
    this.text = _text
    this.completed = _completed
  }
  
  def canEqual(other: Any): Boolean = {
    other.isInstanceOf[com.wonderprints.isomorphic.example.model.Todo]
  }

  override def equals(other: Any): Boolean = {
    other match {
      case that: com.wonderprints.isomorphic.example.model.Todo => Todo.super.equals(that) && that.canEqual(Todo.this) && id == that.id && text == that.text && completed == that.completed
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val prime = 41
    prime * (prime * (prime * Todo.super.hashCode() + id.hashCode) + text.hashCode) + completed.hashCode
  }
}