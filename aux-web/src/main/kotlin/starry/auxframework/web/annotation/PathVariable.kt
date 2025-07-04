package starry.auxframework.web.annotation

import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@RequestInject
@Inherited
annotation class PathVariable(val name: String = "")
