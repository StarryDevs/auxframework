package starry.auxframework.web.service

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import starry.auxframework.context.ConfigurableApplicationContext
import starry.auxframework.context.annotation.Autowired
import starry.auxframework.context.annotation.Import
import starry.auxframework.context.annotation.stereotype.Service
import starry.auxframework.context.bean.ApplicationListener
import starry.auxframework.web.annotation.RequestInject
import starry.auxframework.web.annotation.RequestMapping
import starry.auxframework.web.annotation.RestController
import starry.auxframework.web.configuration.ServerConfiguration
import starry.auxframework.web.handler.*
import java.lang.reflect.ParameterizedType
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

interface WebConfigurator {

    fun configure(connector: EngineConnectorBuilder) {}
    fun configure(application: Application) {}
    fun configure(configuration: NettyApplicationEngine.Configuration) {}

}

typealias EmbeddedWebServer = EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

@Service
@Import(RouterProcessor::class)
class WebService(
    private val configurations: Array<WebConfigurator>,
    private val routerProcessor: RouterProcessor,
    private val beanFactory: ConfigurableApplicationContext,
    private val serverConfiguration: ServerConfiguration,
    private val routingContextHandler: RoutingContextHandler,
) : ApplicationListener {

    private val propertyResolver = beanFactory.propertyResolver

    @Autowired
    private lateinit var responseProcessors: Array<IResponseProcessor<*>>

    @Autowired
    private lateinit var requestInjectProcessors: Array<IRequestInjectProcessor<*>>


    @OptIn(ExperimentalSerializationApi::class)
    val server: EmbeddedWebServer = embeddedServer(
        Netty,
        configure = {
            configurations.forEach { it.configure(this) }
            connectors.add(
                EngineConnectorBuilder().apply {
                    host = serverConfiguration.hostname
                    port = serverConfiguration.port
                    configurations.forEach { it.configure(this) }
                }
            )
        }
    ) {
        configurations.forEach { it.configure(this) }

        routing {
            routerProcessor.restControllers.forEach {
                createRoute(it)
            }
        }
    }

    private fun Route.createRoute(restController: Any): Route? {
        val annotation = restController::class.findAnnotation<RestController>() ?: return null

        val route = if (annotation.path.isEmpty()) this@createRoute else route(annotation.path) {}
        for (method in restController::class.functions) {
            if (method.hasAnnotation<RequestMapping>()) route.routing(restController, method)
        }
        return route
    }

    private suspend fun RoutingContext.handleRequest(restController: Any, method: KFunction<*>) {
        val context = object : IRoutingContext {
            override val call: RoutingCall = this@handleRequest.call
        }
        routingContextHandler.context.set(context)

        val arguments = mutableMapOf<KParameter, Any?>()
        for (parameter in method.parameters) {
            if (parameter.kind == KParameter.Kind.INSTANCE) arguments[parameter] = restController
            else if (parameter.annotations.any { it.annotationClass.hasAnnotation<RequestInject>() || it is RequestInject }) {
                val result = processRequestInject(parameter, context)?.let {
                    propertyResolver.resolve(
                        parameter.type.classifier as KClass<*>,
                        it
                    )
                }
                if (result != null || !parameter.isOptional) arguments[parameter] = result
            } else arguments[parameter] =
                beanFactory.autowire(parameter.type.classifier as KClass<*>, parameter.annotations)
        }

        val response = method.callSuspendBy(arguments)
        if (response != null && response != Unit) {
            processResponse(response, context)
        }

        routingContextHandler.context.remove()
    }

    @Suppress("UNCHECKED_CAST")
    private fun processRequestInject(parameter: KParameter, context: IRoutingContext): Any? {
        val processors = requestInjectProcessors.groupBy {
            val genericInterface = it::class.java.genericInterfaces.filterIsInstance<ParameterizedType>()
                .firstOrNull { that -> that.rawType == IRequestInjectProcessor::class.java }
            genericInterface
                ?.actualTypeArguments?.filterIsInstance<Class<*>>()?.single()
        }.mapKeys { it.key?.kotlin as? KClass<out Annotation> }
        for (annotation in parameter.annotations) {
            for (processor in processors[annotation.annotationClass] ?: continue) {
                val result = (processor as IRequestInjectProcessor<Annotation>).process(parameter, annotation, context)
                if (result.isSome()) return result.getOrNull()
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processResponse(response: Any?, context: IRoutingContext) {
        for (processor in responseProcessors) {
            val genericInterface = processor::class.java.genericInterfaces.filterIsInstance<ParameterizedType>()
                .firstOrNull { it.rawType == IResponseProcessor::class.java }
                ?: continue
            val responseType = genericInterface
                .actualTypeArguments.filterIsInstance<Class<*>>().single()

            if (!responseType.isInstance(response)) continue
            (processor as IResponseProcessor<Any?>).process(response, context)
        }
    }

    private fun Route.routing(restController: Any, method: KFunction<*>) {
        val paths = method.findAnnotations<RequestMapping>().groupBy { it.path }.mapValues {
            it.value.flatMap { that -> that.methods.map { method -> HttpMethod.parse(method) } }.toSet()
        }
        for ((path, methods) in paths) {
            route(path) {
                for (httpMethod in methods) {
                    method(httpMethod) {
                        handle {
                            handleRequest(restController, method)
                        }
                    }
                }
            }
        }
    }

    val serverThread = thread(start = false, name = "aux-web") {
        start()
    }

    override fun finishLoading() {
        if (serverConfiguration.enabled) serverThread.start()
    }

    private fun start() {
        server.start(wait = true)
    }

}

