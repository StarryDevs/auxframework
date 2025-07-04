package starry.auxframework.web.annotation

import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Inherited
annotation class RequestInject
