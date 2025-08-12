package starry.auxframework.util

import java.io.InputStream
import kotlin.reflect.KClass

fun KClass<*>.readResourceAsStream(path: String): InputStream? = java.getResourceAsStream(path)

fun KClass<*>.readResourceAsText(path: String) = readResourceAsStream(path)?.use {
    it.readBytes().decodeToString()
}
