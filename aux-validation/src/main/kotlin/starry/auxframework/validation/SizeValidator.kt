package starry.auxframework.validation

import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.resolve
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.validation.annotation.Size

object SizeValidator : Validator<Size> {

    override fun validate(value: Any?, configuration: Size, propertyResolver: PropertyResolver) {
        if (value == null && configuration.nullable) return
        val size = when (value) {
            is Iterable<*> -> value.count().toLong()
            is Array<*> -> value.size.toLong()
            is CharSequence -> value.length.toLong()
            is Map<*, *> -> value.size.toLong()
            else -> propertyResolver.resolve<String>(value)?.length?.toLong() ?: 0L
        }
        if (size < configuration.min || size > configuration.max) {
            throw ValidationException("Value size $size is not in range [${configuration.min}, ${configuration.max}]")
        }
    }

}
