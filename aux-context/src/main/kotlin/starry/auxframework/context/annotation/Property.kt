package starry.auxframework.context.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
annotation class Property(
    val name: String,
    val type: KClass<*> = Empty::class,
    val isNullable: Nullable = Nullable.DEFAULT
) {
    object Empty
    enum class Nullable {
        YES, NO, DEFAULT
    }
}
