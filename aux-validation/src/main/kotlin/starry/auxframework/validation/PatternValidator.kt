package starry.auxframework.validation

import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.resolve
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.validation.annotation.Pattern

object PatternValidator : Validator<Pattern> {

    override fun validate(value: Any?, configuration: Pattern, propertyResolver: PropertyResolver) {
        val text = propertyResolver.resolve<String>(value)
        if (text == null) {
            if (configuration.nullable) return
            throw ValidationException("Value cannot be null")
        }
        val pattern = configuration.pattern.toRegex(configuration.options.toSet())
        if (!pattern.matches(text)) {
            throw ValidationException("Value '$text' does not match pattern '${configuration.pattern}'")
        }
    }

}
