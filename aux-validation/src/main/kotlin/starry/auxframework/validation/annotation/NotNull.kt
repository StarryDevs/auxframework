package starry.auxframework.validation.annotation

import starry.auxframework.context.annotation.Validated
import starry.auxframework.validation.NotNullValidator

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Validated(NotNullValidator::class)
annotation class NotNull
