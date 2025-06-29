package starry.auxframework.aop

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy
import net.bytebuddy.implementation.InvocationHandlerAdapter
import net.bytebuddy.matcher.ElementMatchers


object ProxyGenerator {

    private val byteBuddy = ByteBuddy()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> createProxy(instance: T, handlers: List<Pair<Int, Any>>): T {
        val beanClass: Class<*> = instance.javaClass
        val sortedHandlers = handlers.sortedBy { it.first }.map { it.second }
        val aroundInvocationHandlers = sortedHandlers
            .filterIsInstance<AroundInvocationHandler>()
        val proxyClass: Class<*> = byteBuddy
            .subclass(beanClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
            .method(ElementMatchers.isPublic())
            .intercept(
                InvocationHandlerAdapter.of { proxy, method, args ->
                    val invocation = MethodInvocation(proxy, instance, method, args ?: arrayOf())
                    for (beforeInvocationHandler in sortedHandlers.filterIsInstance<BeforeInvocationHandler>()) {
                        beforeInvocationHandler.before(invocation)
                    }
                    var option: Option<Any?> = None
                    for (aroundInvocationHandler in aroundInvocationHandlers) {
                        option = aroundInvocationHandler.invoke(invocation)
                        if (option.isSome()) break
                    }
                    var processed = option.getOrElse { invocation.process() }
                    for (afterInvocationHandler in sortedHandlers.filterIsInstance<AfterInvocationHandler>()) {
                        processed = afterInvocationHandler.after(invocation, processed)
                    }
                    processed
                }
            )
            .make()
            .load(beanClass.getClassLoader())
            .loaded
        return proxyClass.getConstructor().newInstance() as T
    }

}