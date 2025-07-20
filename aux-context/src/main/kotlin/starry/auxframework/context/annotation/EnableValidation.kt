package starry.auxframework.context.annotation

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EnableValidation(val enabled: Boolean = true)
