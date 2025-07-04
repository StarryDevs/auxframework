package starry.auxframework.web.annotation

import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@RequestInject
@Inherited
annotation class RequestParam(val name: String = "")
