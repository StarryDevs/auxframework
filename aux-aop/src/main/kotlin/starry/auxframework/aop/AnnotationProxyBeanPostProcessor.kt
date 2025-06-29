package starry.auxframework.aop

import starry.auxframework.context.ConfigurableApplicationContext
import starry.auxframework.context.aware.ConfigurableApplicationContextAware
import starry.auxframework.context.bean.BeanFactory
import starry.auxframework.context.bean.BeanPostProcessor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmName

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
        val annotation = beanClass.findAnnotations(annotationClass).firstOrNull() ?: return instance
        val arguments = annotationClass.memberProperties.map { it.get(annotation) }
        val handlerName = arguments.filterIsInstance<String>().single().takeUnless(String::isEmpty)
        val handlerType = arguments.filterIsInstance<Class<*>>().single()
        val handler = if (handlerName != null) beanFactory.getBean(handlerName) else beanFactory.getBean(handlerType.kotlin)
        val proxy = createProxy(beanClass, instance, handler)
        rawBeanPool[beanName] = instance
        return proxy
    }

    fun createProxy(beanClass: KClass<*>, instance: Any, handler: Any): Any {
        if (handler !is InvocationHandler) {
            throw IllegalArgumentException("Handler for annotation ${annotationClass.jvmName} must implement InvocationHandler")
        }
        return ProxyGenerator.createProxy(instance, handler)
    }


    override fun postProcessOnSetProperty(instance: Any?, beanName: String, beanFactory: BeanFactory): Any? =
        this.rawBeanPool[beanName] ?: instance

}