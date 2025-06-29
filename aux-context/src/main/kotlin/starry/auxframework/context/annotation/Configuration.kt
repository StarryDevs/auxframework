package starry.auxframework.context.annotation

import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Indexed
@Inherited
annotation class Configuration()
