package com.wonderprints.isomorphic.react.model

import scala.beans.BeanProperty

object RenderingData {
  def apply(data: String, content: String) = new RenderingData(data, content)
  def apply(data: String) = new RenderingData(data)
}

class RenderingData(_data: String) {

  @BeanProperty
  val data = s"window.__PRELOADED_STATE__=${_data}"

  @BeanProperty
  var content = ""

  def this(data: String, content: String) = {
    this(data)
    this.content = content
  }

}