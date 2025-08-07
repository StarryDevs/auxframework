package starry.auxframework.context.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class Bean(val name: String = "", val symbol: String = "", val initMethod: String = "", val destroyMethod: String = "")
