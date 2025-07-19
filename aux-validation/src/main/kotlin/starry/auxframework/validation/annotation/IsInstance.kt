package starry.auxframework.validation.annotation

import starry.auxframework.context.annotation.Validated
import starry.auxframework.validation.IsInstanceValidator
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Validated(IsInstanceValidator::class)
annotation class IsInstance(val type: KClass<*>, val nullable: Boolean = false)
