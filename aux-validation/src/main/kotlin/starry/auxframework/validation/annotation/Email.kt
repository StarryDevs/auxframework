package starry.auxframework.validation.annotation

import starry.auxframework.context.annotation.Validated
import starry.auxframework.validation.EmailValidator

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Validated(EmailValidator::class)
annotation class Email(val nullable: Boolean = false)
