package starry.auxframework.context.annotation

import starry.auxframework.context.property.validation.Validator
import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class Validated(val validator: KClass<out Validator<*>>)
