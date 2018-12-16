package com.wonderprints.isomorphic.react.config

import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring5.ISpringWebFluxTemplateEngine
import org.thymeleaf.spring5.view.reactive.ThymeleafReactiveViewResolver

@Configuration
@EnableConfigurationProperties(Array(classOf[ThymeleafProperties]))
class ReactiveThymeleafWebConfig(
    private val templateEngine: ISpringWebFluxTemplateEngine) {

  @Bean
  def thymeleafChunkedAndDataDrivenViewResolver()
    : ThymeleafReactiveViewResolver = {
    val viewResolver =
      new ThymeleafReactiveViewResolver()
    viewResolver.setTemplateEngine(templateEngine)
    viewResolver.setOrder(1)
    // OUTPUT BUFFER size limit
    viewResolver.setResponseMaxChunkSizeBytes(8192)
    viewResolver
  }

}