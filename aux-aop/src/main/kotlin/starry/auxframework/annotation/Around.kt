package starry.auxframework.annotation

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
annotation class Around(val beanName: String = "", val beanClass: KClass<*> = Unit::class)
