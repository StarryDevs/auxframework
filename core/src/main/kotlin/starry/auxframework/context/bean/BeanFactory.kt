package starry.auxframework.context.bean

import kotlin.reflect.KClass

interface BeanFactory : AutoCloseable {

    /**
     * 根据名称获取 Bean
     *
     * @param name Bean 的名称
     * @return Bean 实例
     * @throws NoSuchBeanException 如果没有找到指定名称的 Bean
     */
    fun <T : Any> getBean(name: String): T

    /**
     * 根据类型获取 Bean
     *
     * @param type Bean 的类型
     * @return Bean 实例
     * @throws NoSuchBeanException 如果没有找到指定类型的 Bean
     */
    fun <T : Any> getBean(type: KClass<T>): T

    /**
     * 根据类型获取 Bean
     *
     * @param type Bean 的类型
     * @throws NoSuchBeanException 如果没有找到指定名称和类型的 Bean
     */
    fun <T : Any> getBeans(type: KClass<T>): Set<T>

    /**
     * 判断 Bean 是否存在
     *
     * @param name Bean 的名称
     */
    fun containsBean(name: String): Boolean

    /**
     * 判断 Bean 是否存在
     *
     * @param type Bean 的类型
     */
    fun containsBean(type: KClass<*>): Boolean

    override fun close()

}