package starry.auxframework.web.handler

import starry.auxframework.context.annotation.Configuration
import starry.auxframework.context.bean.BeanFactory
import starry.auxframework.context.bean.BeanPostProcessor
import starry.auxframework.web.annotation.RestController
import kotlin.reflect.full.hasAnnotation

@Configuration
class RouterProcessor : BeanPostProcessor {

    val restControllers = mutableSetOf<Any>()

    override fun postProcessAfterInitialization(instance: Any?, beanName: String, beanFactory: BeanFactory) =
        instance.also {
            if (instance == null || !instance::class.hasAnnotation<RestController>()) return@also

            restControllers += instance
        }

}