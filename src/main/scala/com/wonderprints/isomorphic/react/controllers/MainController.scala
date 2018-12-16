package com.wonderprints.isomorphic.react.controllers

import java.util
import com.wonderprints.isomorphic.react.services.RenderingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MainController {

  @Autowired
  private var renderingService: RenderingService = _

  @RequestMapping(Array("/"))
  def index(model: util.Map[String, Any]): String = {
    // in case we are still rendering, proceed with non-isomorphic rendering
    if (renderingService.isRendering || renderingService
          .renderedPageIsStale()) {
      val rd = renderingService.getModelOnly
      model.put("content", "")
      model.put("data", rd.getData)
      model.put("spinner", " .loader { display: block } ")
      return "index"
    }
    val rd = renderingService.getRenderingData
    if (!rd.isPresent) {
      // The cache should never be empty as we trigger rendering at startup, so this is a sanity check
      return "error"
    }
    model.put("content", rd.get.getContent)
    model.put("data", rd.get.getData)
    model.put("spinner", " .loader { display: none } ")
    "index"
  }

}
