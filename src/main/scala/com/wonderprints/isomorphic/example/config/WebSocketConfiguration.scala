package com.wonderprints.isomorphic.example.config

import java.util

import com.wonderprints.isomorphic.example.services.ClientMessageDecoder
import com.wonderprints.isomorphic.example.websocket.ReactiveWebSocketHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import com.wonderprints.isomorphic.react.services.RenderingService

@Configuration
@Autowired
class WebSocketConfiguration(private val clientMessageDecoder: ClientMessageDecoder, private val renderingService: RenderingService) {
  
  @Bean
  def handlerAdapter(): WebSocketHandlerAdapter = new WebSocketHandlerAdapter()

  @Bean
  def wsh(): WebSocketHandler = ReactiveWebSocketHandler(clientMessageDecoder, renderingService)

  @Bean
  def hm(): HandlerMapping = {
    val handlerMapping = new SimpleUrlHandlerMapping()
    handlerMapping.setOrder(10)
    val map =
      new util.HashMap[String, WebSocketHandler]()
    map.put("/react", wsh())
    handlerMapping.setUrlMap(map)
    handlerMapping
  }

}