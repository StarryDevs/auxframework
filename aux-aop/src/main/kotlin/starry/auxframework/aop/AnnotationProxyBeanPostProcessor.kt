package starry.auxframework.aop

import starry.auxframework.annotation.Aspect
import starry.auxframework.context.ConfigurableApplicationContext
import starry.auxframework.context.aware.ConfigurableApplicationContextAware
import starry.auxframework.context.bean.BeanFactory
import starry.auxframework.context.bean.BeanPostProcessor
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties

abstract class AnnotationProxyBeanPostProcessor<T : Annotation> : BeanPostProcessor, ConfigurableApplicationContextAware {

    @Suppress("UNCHECKED_CAST")
    protected val annotationClass = (this::class.java.genericSuperclass as ParameterizedType)
        .actualTypeArguments
        .filterIsInstance<Class<T>>()
        .single()
        .kotlin

    protected val rawBeanPool = mutableMapOf<String, Any>()

    override fun setConfigurableApplicationContext(context: ConfigurableApplicationContext) {
        appContext = context
    }
    protected lateinit var appContext: ConfigurableApplicationContext

    @Suppress("UNCHECKED_CAST")
    override fun postProcessBeforeInitialization(instance: Any?, beanName: String, beanFactory: BeanFactory): Any? {
        if (instance == null) return null
        val beanClass = instance::class
        val aspect = beanClass.findAnnotation<Aspect>() ?: return instance
        if (!aspect.enabled) return instance
        val handlers = beanClass.findAnnotations(annotationClass)
            .map { annotation ->
                val arguments = annotationClass.memberProperties.map { it.get(annotation) }
                val order = arguments.filterIsInstance<Int>().singleOrNull() ?: -1
                val handlerName = arguments.filterIsInstance<String>().singleOrNull()?.takeUnless(String::isEmpty)
                val handlerType = arguments.filterIsInstance<Class<*>>().singleOrNull()
                order to if (handlerName != null) beanFactory.getBean(handlerName) else beanFactory.getBean(handlerType!!.kotlin)
            }
            .toList()
        if (handlers.isEmpty()) return instance
        val proxy = createProxy(beanClass, instance, handlers)
        rawBeanPool[beanName] = instance
        return proxy
    }

    fun createProxy(beanClass: KClass<*>, instance: Any, handlers: List<Pair<Int, Any>>): Any {
        return ProxyGenerator.createProxy(instance, handlers)
    }


    override fun postProcessOnSetProperty(instance: Any?, beanName: String, beanFactory: BeanFactory): Any? =
        this.rawBeanPool[beanName] ?: instance

}