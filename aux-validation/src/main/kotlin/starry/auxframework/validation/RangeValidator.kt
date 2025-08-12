package starry.auxframework.validation

import starry.akarui.core.chars.CharSource
import starry.akarui.core.operator.parse
import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.resolve
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.util.math.BigDecimalRange
import starry.auxframework.validation.annotation.Range
import java.math.BigDecimal
import java.math.BigInteger

object RangeValidator : Validator<Range> {

    override fun validate(value: Any?, configuration: Range, propertyResolver: PropertyResolver) {
        if (value == null) {
            if (!configuration.nullable) {
                throw ValidationException("Value cannot be null")
            }
            return
        }
        val range = try {
            CharSource.wrap(configuration.expression).parse(BigDecimalRange.Parser)
        } catch (exception: Throwable) {
            throw ValidationException("Invalid range expression: ${configuration.expression}", exception)
        }
        val number = when (value) {
            is Byte -> value.toInt().toBigDecimal()
            is Short -> value.toInt().toBigDecimal()
            is Int -> value.toBigDecimal()
            is Long -> value.toBigDecimal()
            is Float -> value.toBigDecimal()
            is Double -> value.toBigDecimal()
            is BigDecimal -> value
            is BigInteger -> value.toBigDecimal()
            is String -> value.toBigDecimalOrNull() ?: throw IllegalArgumentException("Value must be a number")
            else -> propertyResolver.resolve<String>(value)?.toBigDecimalOrNull()
                ?: throw ValidationException("Value must be a number or a string representing a number")
        }
        if (number !in range) {
            throw ValidationException("Value $value is not in range $range")
        }
    }

}
