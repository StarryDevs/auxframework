package starry.auxframework.expression.property

import starry.auxframework.context.property.PropertyExpression
import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.resolve
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.full.memberFunctions

class PropertyNegateExpression(private val expression: PropertyExpression) : PropertyExpression {

    override fun resolve(properties: PropertyResolver): Any? {
        val obj = expression.resolve(properties) ?: return null
        val negateFunction = obj::class.memberFunctions.singleOrNull { (it.name == "negate" || it.name == "unaryMinus") && it.parameters.size == 1 }
        if (negateFunction != null) {
            return negateFunction.call(obj)
        }
        return when (obj) {
            is Int -> -obj
            is Long -> -obj
            is Short -> -obj
            is Byte -> -obj
            is Float -> -obj
            is Double -> -obj
            is BigDecimal -> obj.negate()
            is BigInteger -> obj.negate()
            else -> properties.resolve<BigDecimal>(obj)?.negate()
        }
    }

}
