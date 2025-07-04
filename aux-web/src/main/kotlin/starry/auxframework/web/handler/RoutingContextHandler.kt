package starry.auxframework.web.handler

import io.ktor.server.routing.*
import starry.auxframework.context.annotation.Configuration
import starry.auxframework.context.bean.ProxyBeanInvocationHandler

interface IRoutingContext {
    val call: RoutingCall
}

@Configuration
class RoutingContextHandler : ProxyBeanInvocationHandler<IRoutingContext> {

    val context = ThreadLocal<IRoutingContext>()

    override fun getObject(): IRoutingContext? = context.get()

}