package com.wonderprints.isomorphic.react.renderer

import jdk.nashorn.api.scripting.NashornScriptEngine
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.Executors

class React {

  private val engineHolder =
    ThreadLocal.withInitial(() => {
      val nashornScriptEngine = new ScriptEngineManager()
        .getEngineByName("nashorn")
        .asInstanceOf[NashornScriptEngine]
      val globalScheduledThreadPool =
        Executors.newScheduledThreadPool(20)
      val ctx = new SimpleScriptContext()
      ctx.setAttribute("__NASHORN_POLYFILL_TIMER__",
                       globalScheduledThreadPool,
                       ScriptContext.ENGINE_SCOPE)
      ctx.setAttribute("__HTTP_SERVLET_REQUEST__",
                       globalScheduledThreadPool,
                       ScriptContext.ENGINE_SCOPE)
      nashornScriptEngine.setContext(ctx)
      nashornScriptEngine.eval(read("static/nashorn-polyfill.js"))
      nashornScriptEngine.eval(read("static/server.js"))
      nashornScriptEngine
    })

  def render(initialState: String, request: String): String = {
    val html: AnyRef =
      engineHolder.get.invokeFunction("render", initialState, request)
    String.valueOf(html)
  }

  private def read(path: String): Reader =
    new InputStreamReader(getClass.getClassLoader.getResourceAsStream(path))

}
