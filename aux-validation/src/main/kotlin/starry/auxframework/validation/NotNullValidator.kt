package starry.auxframework.validation

import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.validation.annotation.NotNull

object NotNullValidator : Validator<NotNull> {

    override fun validate(value: Any?, configuration: NotNull, propertyResolver: PropertyResolver) {
        if (value == null) {
            throw ValidationException("Value cannot be null")
        }
    }

}
