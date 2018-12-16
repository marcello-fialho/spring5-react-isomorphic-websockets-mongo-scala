package com.wonderprints.isomorphic.example.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import scala.beans.BeanProperty

object VisibilityFilter {
  def apply(value: String) = new VisibilityFilter(value)
}

@Document
class VisibilityFilter extends Equals {
  @Id @BeanProperty var value: String = _
  
  def this(value: String) = {
    this()
    this.value = value
  }
  
  def canEqual(other: Any): Boolean = {
    other.isInstanceOf[com.wonderprints.isomorphic.example.model.VisibilityFilter]
  }

  override def equals(other: Any): Boolean = {
    other match {
      case that: com.wonderprints.isomorphic.example.model.VisibilityFilter => VisibilityFilter.super.equals(that) && that.canEqual(VisibilityFilter.this) && value == that.value
      case _ => false
    }
  }

  override def hashCode(): Int = {
    val prime = 41
    prime * VisibilityFilter.super.hashCode() + value.hashCode
  }
}