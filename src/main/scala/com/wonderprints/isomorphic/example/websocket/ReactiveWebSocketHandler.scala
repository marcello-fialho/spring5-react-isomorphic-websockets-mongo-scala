package com.wonderprints.isomorphic.example.websocket

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import com.wonderprints.isomorphic.example.services.ClientMessageDecoder
import com.wonderprints.isomorphic.example.websocket.ReactiveWebSocketHandler._
import com.wonderprints.isomorphic.react.services.RenderingService
import org.springframework.web.reactive.socket.{WebSocketHandler, WebSocketMessage, WebSocketSession}
import reactor.core.publisher.{FluxSink, Mono, SignalType}
import reactor.core.scala.publisher.Flux
import reactor.core.scheduler.Schedulers

object ReactiveWebSocketHandler {
  private val sessionsMap =
    new ConcurrentHashMap[String, FluxSink[String]]()

  def broadcast(message: String): Unit = {
    sessionsMap.values.forEach(sink => sink.next(message))
  }

  def apply(clientMessageDecoder: ClientMessageDecoder, renderingService: RenderingService) = new ReactiveWebSocketHandler(clientMessageDecoder, renderingService)
}

class ReactiveWebSocketHandler(private val clientMessageDecoder: ClientMessageDecoder,
                               private val renderingService: RenderingService) extends WebSocketHandler {

  override def handle(webSocketSession: WebSocketSession): Mono[Void] = {
    val sessionId = webSocketSession.getId
    // Subscribe to the inbound message flux
    Flux(webSocketSession
      .receive()
      .doFinally((sig: SignalType) => {
        println(s"Terminating WebSocket Session (client side) sig: [${sig.name()}] [$sessionId]")
        webSocketSession.close()
        // remove the stored session id
        sessionsMap.remove(sessionId)
      }))
      .map((inMsg: WebSocketMessage) => inMsg.getPayloadAsText.replace("\\", ""))
      .filter((arg: String) => arg.length >= 2)
      .map((text: String) => text.substring(1, text.length - 1))
      .flatMap(message => clientMessageDecoder.handleMessage(message).subscribeOn(Schedulers.elastic()))
      .flatMap(message => {
        broadcast(message)
        Mono.just(message)
      })
      .doOnNext(message => println(s"Received message: $message"))
      .onBackpressureLatest()
      .flatMap((message: String) => renderingService.getCurrentStateAsString$.flatMap((stateAsString: String) => {
        renderingService.setCurrentStateAsString(stateAsString)
        Mono.just(message)
      }))
      .flatMap((message: String) => {
        val now = Instant.now().toEpochMilli
        renderingService.render();
        println(s"rendered in ${Instant.now().toEpochMilli - now} milliseconds")
        Mono.just(message)
      })
      .subscribe((_: Any) => {})
    webSocketSession.send(getFlux(sessionId).map(message =>
      webSocketSession.textMessage(message)))
  }

  def getFlux(sessionId: String): Flux[String] = {
    Flux.create(sink => {
      sessionsMap.put(sessionId, sink)
      println("added one session")
    })
  }

}