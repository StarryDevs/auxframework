package starry.auxframework.context

import starry.auxframework.context.bean.BeanDefinition
import starry.auxframework.context.bean.BeanFactory
import starry.auxframework.context.property.AutowireOptions
import starry.auxframework.context.property.PropertyResolver
import kotlin.reflect.KClass


abstract class ConfigurableApplicationContext : BeanFactory {

    abstract val propertyResolver: PropertyResolver


    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     */
    abstract fun <T : Any> findBeanDefinitions(type: KClass<T>, symbol: String? = null): Set<BeanDefinition>

    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     */
    open fun findBeanDefinition(type: KClass<*>, symbol: String? = null): BeanDefinition? =
        findBeanDefinitions(type, symbol)
            .singleOrNull()

    /**
     * 根据名称获取 Bean 定义
     *
     * @param name Bean 的名称
     */
    abstract fun findBeanDefinition(name: String): BeanDefinition?

    override fun containsBean(name: String) = findBeanDefinition(name) != null
    override fun containsBean(type: KClass<*>, symbol: String?) = findBeanDefinition(type, symbol) != null

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBean(name: String): T = findBeanDefinition(name)?.instanceObject as? T
        ?: throw IllegalArgumentException("No bean found with name: $name")

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBeans(type: KClass<T>, symbol: String?) =
        findBeanDefinitions(type, symbol).mapNotNull { it.instanceObject as? T }.toSet()

    override fun <T : Any> getBean(type: KClass<T>, symbol: String?): T = getBeans(type, symbol)
        .single()

    abstract fun load()


    /**
     * 创建单例对象 Bean
     *
     * @param singleton 单例对象
     * @param name Bean 名称 (选填，默认自动生成)
     */
    abstract fun registerSingleton(singleton: Any, name: String? = null, symbol: String? = null): BeanDefinition

    /**
     * 自动填充
     */
    abstract fun autowire(
        type: KClass<*>,
        annotations: List<Annotation>,
        autowireOptions: AutowireOptions = AutowireOptions()
    ): Any?

}
