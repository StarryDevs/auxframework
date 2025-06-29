package starry.auxframework.annotation

import java.lang.annotation.Inherited

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Inherited
annotation class Aspect(val enabled: Boolean = true)
