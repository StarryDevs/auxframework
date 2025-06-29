package starry.auxframework.util

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

fun <T : Annotation> findAnnotation(target: KAnnotatedElement, annotationClass: KClass<T>): T? {
    val topAnnotation = target.findAnnotations(annotationClass).singleOrNull()
    if (topAnnotation != null) return topAnnotation
    for (annotation in target.annotations) {
        val annotationType = annotation.annotationClass
        if (annotationType.java.packageName != "java.lang.annotation" && annotationType.java.packageName != "kotlin.annotation") {
            val found = findAnnotation(annotationType, annotationClass) ?: continue
            return found
        }
    }
    return null
}
