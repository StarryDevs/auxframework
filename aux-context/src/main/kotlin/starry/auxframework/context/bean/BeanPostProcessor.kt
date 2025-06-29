package starry.auxframework.context.bean

interface BeanPostProcessor {

    fun postProcessBeforeInitialization(instance: Any?, beanName: String, beanFactory: BeanFactory): Any? = instance
    fun postProcessOnSetProperty(instance: Any?, beanName: String, beanFactory: BeanFactory): Any? = instance

}