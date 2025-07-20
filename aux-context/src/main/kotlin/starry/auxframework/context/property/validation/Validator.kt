package starry.auxframework.context.property.validation

import starry.auxframework.context.annotation.EnableValidation
import starry.auxframework.context.annotation.Validated
import starry.auxframework.context.property.PropertyResolver
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

interface Validator<A : Annotation> {

    companion object {

        fun fromAnnotations(annotations: List<Annotation>): Map<Validator<out Annotation>, List<Annotation>> {
            val enableValidation = annotations.filterIsInstance<EnableValidation>().singleOrNull()
            if (enableValidation?.enabled == false) return emptyMap()
            return annotations
                .mapNotNull { annotation ->
                    annotation.annotationClass.findAnnotation<Validated>()?.let { annotation to it }
                }
                .groupBy { it.second.validator }
                .mapValues { it.value.map { (first) -> first } }
                .mapKeys { (key) -> key.objectInstance ?: key.createInstance() }
        }

        @Throws(ValidationException::class)
        fun check(value: Any?, validators: Map<Validator<out Annotation>, List<Annotation>>, propertyResolver: PropertyResolver) {
            for ((validator, annotations) in validators) {
                val validator = validator as Validator<Annotation>
                for (annotation in annotations) {
                    validator.validate(value, annotation, propertyResolver)
                }
            }
        }

    }

    @Throws(ValidationException::class)
    fun validate(value: Any?, configuration: A, propertyResolver: PropertyResolver)

}
