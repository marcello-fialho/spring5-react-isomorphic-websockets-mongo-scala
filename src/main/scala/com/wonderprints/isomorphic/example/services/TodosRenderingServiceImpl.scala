package com.wonderprints.isomorphic.example.services

import java.util
import com.wonderprints.isomorphic.example.model.Todo
import com.wonderprints.isomorphic.example.model.VisibilityFilter
import com.wonderprints.isomorphic.example.repositories.TodoRepository
import com.wonderprints.isomorphic.example.repositories.VisibilityFilterRepository
import com.wonderprints.isomorphic.react.model.RenderingData
import com.wonderprints.isomorphic.react.services.RenderingService
import com.wonderprints.isomorphic.react.services.RenderingServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct
import java.util.Optional
import java.util.function.BiFunction
import java.util.function.Supplier

@Service("renderingService")
@Autowired
class TodosRenderingServiceImpl(private val todoRepository: TodoRepository,
                                private val visibilityFilterRepository: VisibilityFilterRepository)  extends RenderingService {

  @Value("${rendering-wait-timeout}")
  private var renderingWaitTimeoutStr: String = _

  private def reducer[T]: BiFunction[util.ArrayList[T], T, util.ArrayList[T]] = (acc: util.ArrayList[T], curr: T) => {
    acc.add(curr)
    acc
  }

  private val stateGetter$: BiFunction[TodoRepository,
    VisibilityFilterRepository,
    Mono[util.Map[String, Any]]] =
    (todoRepository: TodoRepository,
     visibilityFilterRepository: VisibilityFilterRepository) =>
      todoRepository
        .findAll()
        .reduce(new util.ArrayList[Todo](), reducer)
        .flatMap((todosList: util.ArrayList[Todo]) => {
          val initialState: util.HashMap[String, Any] = new util.HashMap[String, Any]()
          initialState.put("todos", todosList)
          Mono.just(initialState)
        })
        .flatMap(
          (initialState: util.HashMap[String, Any]) =>
            visibilityFilterRepository
              .findAll()
              .reduce(new util.ArrayList[VisibilityFilter](), this.reducer)
              .flatMap(vList => {
                initialState.put("visibilityFilter", vList.get(0).getValue)
                Mono.just(initialState)
              }))

  private var renderingServiceImpl: RenderingServiceImpl = _

  override def getRenderingData: Optional[RenderingData] =
    renderingServiceImpl.getRenderingData

  override def getModelOnly: RenderingData =
    renderingServiceImpl.getModelOnly

  override def renderedPageIsStale(): Boolean =
    renderingServiceImpl.renderedPageIsStale()

  override def getCurrentStateAsString$: Mono[String] =
    renderingServiceImpl.getCurrentStateAsString$

  override def setCurrentStateAsString(currentStateAsString: String): Unit = {
    renderingServiceImpl.setCurrentStateAsString(currentStateAsString)
  }

  private val getPropsIntegerValue: BiFunction[Supplier[String],Integer,Integer] =
    (propsGetter: Supplier[String], defaultValue: java.lang.Integer) =>
      try java.lang.Integer.parseInt(propsGetter.get)
      catch {
        case _: NumberFormatException => defaultValue
      }

  private def getRenderingWaitTimeout: java.lang.Integer =
    getPropsIntegerValue.apply(() => renderingWaitTimeoutStr, 10000)

  @PostConstruct
  private def start(): Unit = {
    renderingServiceImpl = new RenderingServiceImpl(stateGetter$, todoRepository, visibilityFilterRepository)
    renderingServiceImpl.setRenderingWaitTimeout(getRenderingWaitTimeout)
  }

  override def render(): Unit = {
    renderingServiceImpl.render()
  }

  override def render(url: String): Unit = {
    renderingServiceImpl.render(url)
  }

  override def isRendering: Boolean = renderingServiceImpl.isRendering

  override def tryWaitUntilRendered(): Boolean =
    renderingServiceImpl.tryWaitUntilRendered()

  override def init(): Unit = renderingServiceImpl.init()
}
