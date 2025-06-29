package starry.auxframework.aop

import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy
import net.bytebuddy.implementation.InvocationHandlerAdapter
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.InvocationHandler


object ProxyGenerator {

    private val byteBuddy = ByteBuddy()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> createProxy(instance: T, handler: InvocationHandler): T {
        val beanClass: Class<*> = instance.javaClass
        val proxyClass: Class<*> = byteBuddy
            .subclass(beanClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
            .method(ElementMatchers.isPublic())
            .intercept(InvocationHandlerAdapter.of { proxy, method, args -> handler.invoke(instance, method, args) })
            .make()
            .load(beanClass.getClassLoader())
            .loaded
        return proxyClass.getConstructor().newInstance() as T
    }

}