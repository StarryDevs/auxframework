package starry.auxframework.validation

import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.validation.annotation.IsInstance

object IsInstanceValidator : Validator<IsInstance> {

    override fun validate(value: Any?, configuration: IsInstance, propertyResolver: PropertyResolver) {
        if (value == null) {
            if (!configuration.nullable) {
                throw ValidationException("Value cannot be null")
            }
            return
        }

        if (!configuration.type.isInstance(value)) {
            throw ValidationException("Value of type ${value::class} is not an instance of ${configuration.type}")
        }
    }

}
