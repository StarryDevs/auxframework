package starry.auxframework.context.bean

import starry.auxframework.context.annotation.PostConstruct
import starry.auxframework.context.annotation.PreDestroy
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation

class BeanDefinition(
    val name: String,
    val beanClass: KClass<*>,
    var instance: Any? = null,
    val constructor: KCallable<*>? = null,
    private val initMethodName: String? = null,
    private val destroyMethodName: String? = null,
    private val initMethod: KFunction<*>? = null,
    private val destroyMethod: KFunction<*>? = null
) {

    var order: Int = -1

    var constructed: Boolean = false
    var propertySet: Boolean = false

    fun getInitMethod(): KFunction<*>? = initMethod
        ?: initMethodName?.let { name -> beanClass.members.firstOrNull { it.name == name } as? KFunction<*> }
        ?: (instance?.let { it::class } ?: beanClass).members.firstOrNull { it.hasAnnotation<PostConstruct>() } as? KFunction<*>

    fun getDestroyMethod(): KFunction<*>? = destroyMethod
        ?: destroyMethodName?.let { name -> beanClass.members.firstOrNull { it.name == name } as? KFunction<*> }
        ?: (instance?.let { it::class } ?: beanClass).members.firstOrNull { it.hasAnnotation<PreDestroy>() } as? KFunction<*>

    override fun hashCode() = name.hashCode()
    override fun equals(other: Any?) = other != null && other is BeanDefinition && other.name == name

    override fun toString(): String {
        return "BeanDefinition(name='$name', instance=$instance, order=$order)"
    }

}