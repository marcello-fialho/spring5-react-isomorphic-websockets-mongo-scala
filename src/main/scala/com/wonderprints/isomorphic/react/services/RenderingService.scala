package com.wonderprints.isomorphic.react.services

import com.wonderprints.isomorphic.react.model.RenderingData
import reactor.core.publisher.Mono
import java.util.Optional

trait RenderingService {

  def getRenderingData: Optional[RenderingData]
  def getModelOnly: RenderingData
  def render(): Unit
  def render(url: String): Unit
  def isRendering: Boolean
  def tryWaitUntilRendered(): Boolean
  def renderedPageIsStale(): Boolean
  def getCurrentStateAsString$: Mono[String]
  def setCurrentStateAsString(currentStateAsString: String): Unit
  def init(): Unit

}
