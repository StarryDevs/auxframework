package starry.auxframework.context.annotation.stereotype

import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Indexed
@Inherited
annotation class Component
