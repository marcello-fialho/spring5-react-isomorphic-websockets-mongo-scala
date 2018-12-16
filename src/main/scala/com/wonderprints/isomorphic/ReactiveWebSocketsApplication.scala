package com.wonderprints.isomorphic

import com.wonderprints.isomorphic.react.services.RenderingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener


object ReactiveWebSocketsApplication {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[ReactiveWebSocketsApplication], args: _*)
  }
}

@SpringBootApplication
@Autowired
class ReactiveWebSocketsApplication(private val renderingService: RenderingService) {

  @EventListener(Array(classOf[ApplicationReadyEvent]))
  def render(): Unit = {
    renderingService.init()
    renderingService.render()
  }

}