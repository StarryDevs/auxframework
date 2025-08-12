package starry.auxframework.expression.property

import starry.akarui.core.chars.CharParser
import starry.akarui.core.chars.symbol
import starry.akarui.core.chars.whitespace
import starry.akarui.core.operator.unaryPlus
import starry.auxframework.context.property.ConstantPropertyExpression
import starry.auxframework.context.property.PropertyParser
import starry.auxframework.expression.common.number
import starry.auxframework.util.IBootstrap

object PropertyExpressionParser : IBootstrap {

    val positive = CharParser.sequence("Positive") {
        +symbol("+")
        +whitespace
        +PropertyParser.property
    }

    val negative = CharParser.sequence("Negative") {
        +symbol("-")
        +whitespace
        PropertyNegateExpression(+PropertyParser.property)
    }

    val numberConstant = CharParser.sequence("Positive") {
        ConstantPropertyExpression(+number)
    }

    init {
        PropertyParser.properties.add(positive)
        PropertyParser.properties.add(negative)
        PropertyParser.properties.add(numberConstant)
    }

}
