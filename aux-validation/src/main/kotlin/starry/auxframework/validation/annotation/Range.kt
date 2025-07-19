package starry.auxframework.validation.annotation

import starry.auxframework.context.annotation.Validated
import starry.auxframework.validation.RangeValidator

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Validated(RangeValidator::class)
annotation class Range(val expression: String, val nullable: Boolean = false)
