package starry.auxframework.validation.annotation

import starry.auxframework.context.annotation.Validated
import starry.auxframework.validation.SizeValidator

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Validated(SizeValidator::class)
annotation class Size(val min: Long = Long.MIN_VALUE, val max: Long = Long.MAX_VALUE, val nullable: Boolean = false)
