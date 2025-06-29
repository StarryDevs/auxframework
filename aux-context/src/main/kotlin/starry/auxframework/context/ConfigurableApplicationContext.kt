package starry.auxframework.context

import starry.auxframework.context.bean.BeanDefinition
import starry.auxframework.context.bean.BeanFactory
import kotlin.reflect.KClass


abstract class ConfigurableApplicationContext : BeanFactory {


    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     */
    abstract fun <T : Any> findBeanDefinitions(type: KClass<T>): Set<BeanDefinition>

    /**
     * 根据类型获取 Bean 定义
     *
     * @param type Bean 的类型
     */
    open fun findBeanDefinition(type: KClass<*>): BeanDefinition? = findBeanDefinitions(type).singleOrNull()

    /**
     * 根据名称获取 Bean 定义
     *
     * @param name Bean 的名称
     */
    abstract fun findBeanDefinition(name: String): BeanDefinition?

    override fun containsBean(name: String) = findBeanDefinition(name) != null
    override fun containsBean(type: KClass<*>) = findBeanDefinition(type) != null

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBean(name: String): T
        = findBeanDefinition(name)?.getInstance() as? T ?: throw IllegalArgumentException("No bean found with name: $name")

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getBeans(type: KClass<T>) = findBeanDefinitions(type).mapNotNull { it.getInstance() as? T }.toSet()

    override fun <T : Any> getBean(type: KClass<T>): T = getBeans(type).single()

}