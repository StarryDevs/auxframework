package starry.auxframework.context.annotation

@Retention(AnnotationRetention.RUNTIME)
annotation class Value(val expression: String, val isRaw: Boolean = false)
