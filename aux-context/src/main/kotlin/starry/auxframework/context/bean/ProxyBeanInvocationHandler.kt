package starry.auxframework.context.bean

import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy
import net.bytebuddy.implementation.InvocationHandlerAdapter
import net.bytebuddy.matcher.ElementMatchers
import starry.auxframework.context.ConfigurableApplicationContext
import starry.auxframework.context.annotation.Configuration
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import kotlin.reflect.jvm.jvmName

interface ProxyBeanInvocationHandler<T> : InvocationHandler {

    fun getObject(): T?

    override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
        val args = args ?: arrayOf()
        val obj = getObject()
        return when (method.name) {
            "hashCode" -> System.identityHashCode(proxy)
            "toString" -> obj.toString()
            "equals" -> obj === proxy
            else -> method.invoke(obj, *args)
        }
    }

}

@Configuration
class ProxyBeanInvocationHandlerProcessor : BeanPostProcessor {

    val byteBuddy = ByteBuddy()

    override fun postProcessBeforeInitialization(instance: Any?, beanName: String, beanFactory: BeanFactory): Any? =
        instance.also {
            if (instance == null || instance !is ProxyBeanInvocationHandler<*>) return@also
            if (beanFactory !is ConfigurableApplicationContext) return@also

            val genericInterface = instance::class.java.genericInterfaces.filterIsInstance<ParameterizedType>()
                .firstOrNull { it.rawType == ProxyBeanInvocationHandler::class.java }
                ?: return@also
            val targetClass = genericInterface
                .actualTypeArguments.filterIsInstance<Class<*>>().single()
            val proxyClass: Class<*> = byteBuddy
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                .method(ElementMatchers.isPublic())
                .intercept(InvocationHandlerAdapter.of(instance))
                .make()
                .load(targetClass.getClassLoader())
                .loaded
            val proxyObject = proxyClass.getConstructor().newInstance()

            beanFactory.registerSingleton(proxyObject, "${targetClass.kotlin.jvmName}@${instance::class.jvmName}")
        }

}

