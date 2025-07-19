package starry.auxframework.context.property.validation

import starry.auxframework.context.property.PropertyResolver

interface Validator<A : Annotation> {

    @Throws(ValidationException::class)
    fun validate(value: Any?, configuration: A, propertyResolver: PropertyResolver)

}
