package starry.auxframework.annotation

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@Repeatable
annotation class Agent(val type: KClass<*> = Unit::class, val name: String = "" , val order: Int = -1)
