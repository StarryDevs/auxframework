package starry.auxframework.validation.annotation

import starry.auxframework.context.annotation.Validated
import starry.auxframework.validation.PatternValidator

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Validated(PatternValidator::class)
annotation class Pattern(val pattern: String, vararg val options: RegexOption, val nullable: Boolean = false)
