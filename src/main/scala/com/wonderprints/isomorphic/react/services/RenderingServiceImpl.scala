package com.wonderprints.isomorphic.react.services

import java.util
import java.util.Optional
import java.util.concurrent._
import java.util.function.{BiFunction, Consumer}

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.wonderprints.isomorphic.example.repositories.{TodoRepository, VisibilityFilterRepository}
import com.wonderprints.isomorphic.react.model.RenderingData
import com.wonderprints.isomorphic.react.renderer.React
import reactor.core.publisher.Mono

import scala.beans.BooleanBeanProperty

class RenderingServiceImpl(private val stateGetter$: BiFunction[TodoRepository,VisibilityFilterRepository, Mono[util.Map[String, Any]]],
                           private val todoRepository: TodoRepository,
                           private val visibilityFilterRepository: VisibilityFilterRepository)
  extends RenderingService {

  private var cache: RenderingData = _

  private val react: React = new React()

  private val mapper: ObjectMapper = new ObjectMapper()

  @volatile private var lastRenderedStateAsString: String = ""

  @volatile private var currentStateAsString: String = ""

  private val sem: Semaphore = new Semaphore(1)

  def init(): Unit = {
    currentStateAsString = getCurrentStateAsStringSync
  }

  def setCurrentStateAsString(currentStateAsString: String): Unit = {
    synchronized {
      this.currentStateAsString = currentStateAsString
    }
  }

  def getCurrentStateAsString$: Mono[String] =
    try stateGetter$
      .apply(todoRepository, visibilityFilterRepository)
      .flatMap((state: util.Map[String, Any]) =>
        try {
          val currentStateAsString: String = mapper.writeValueAsString(state)
          Mono.just(currentStateAsString)
        } catch {
          case e: JsonProcessingException => {
            e.printStackTrace()
            Mono.just("")
          }
        })
    catch {
      case e: Exception => {
        e.printStackTrace()
        Mono.empty()
      }

    }

  private def getCurrentStateAsStringSync: String = {
    val sem: Semaphore = new Semaphore(1)
    val currentStateAsStringBuilder: StringBuilder = new StringBuilder()
    sem.acquireUninterruptibly()
    getCurrentStateAsString$.subscribe((stateAsString: String) => {
      currentStateAsStringBuilder.append(stateAsString)
      sem.release()
    }, (error: Throwable) => {
      error.printStackTrace()
      sem.release()
    }, () => {
      sem.release()
    })
    sem.acquireUninterruptibly()
    sem.release()
    currentStateAsStringBuilder.toString
  }

  def setRenderingWaitTimeout(renderingWaitTimeout: Int): Unit = {
    this.renderingWaitTimeout = renderingWaitTimeout
  }

  private var renderingWaitTimeout: Int = _

  @BooleanBeanProperty
  var rendering: Boolean = false

  override def getRenderingData: Optional[RenderingData] = {
    if (cache == null) Optional.empty()
    try {
      val acquired: Boolean =
        sem.tryAcquire(renderingWaitTimeout, TimeUnit.MILLISECONDS)
      if (acquired) {
        sem.release()
        Optional.of(cache)
      } else {
        Optional.empty()
      }
    } catch {
      case _: InterruptedException => Optional.empty()

    }
  }

  override def getModelOnly: RenderingData =
    new RenderingData(currentStateAsString)

  override def tryWaitUntilRendered(): Boolean =
    try {
      val acquired: Boolean =
        sem.tryAcquire(renderingWaitTimeout, TimeUnit.MILLISECONDS)
      if (acquired) {
        sem.release()
        true
      } else {
        false
      }
    } catch {
      case _: InterruptedException => false

    }

  override def renderedPageIsStale(): Boolean =
    currentStateAsString != lastRenderedStateAsString

  override def render(): Unit = {
    render("/")
  }

  override def render(url: String): Unit = {
    sem.acquireUninterruptibly()
    rendering = true
    val req: util.HashMap[String, Any] = new util.HashMap[String, Any]()
    req.put("location", url)
    try {
      val requestAsString: String = mapper.writeValueAsString(req)
      val initialStateAsString: String = currentStateAsString
      lastRenderedStateAsString = initialStateAsString
      val content: String = react.render(initialStateAsString, requestAsString)
      cache = new RenderingData(initialStateAsString, content)
      println("Template rendered...")
    } catch {
      case e: JsonProcessingException =>
        throw new RuntimeException("failed to parse json input(s)", e)
    } finally {
      sem.release()
      rendering = false
    }
  }

}
