package starry.auxframework.context.annotation

import starry.auxframework.context.annotation.stereotype.Component
import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
@Inherited
annotation class Configuration()