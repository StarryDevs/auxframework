package starry.auxframework.expression.property

import starry.adventure.parser.symbol
import starry.adventure.parser.util.rule
import starry.adventure.parser.whitespace
import starry.auxframework.context.property.ConstantPropertyExpression
import starry.auxframework.context.property.PropertyParser
import starry.auxframework.expression.common.number
import starry.auxframework.expression.common.singleLineSqString
import starry.auxframework.util.IBootstrap

object PropertyExpressionParser : IBootstrap {

    val positive by rule {
        +symbol("+")
        +whitespace
        +PropertyParser.property
    }

    val negative by rule {
        +symbol("-")
        +whitespace
        PropertyNegateExpression(+PropertyParser.property)
    }

    val singleLineSqStringConstant by rule {
        ConstantPropertyExpression(+singleLineSqString)
    }

    val numberConstant by rule {
        ConstantPropertyExpression(+number)
    }

    init {
        PropertyParser.properties.add(positive)
        PropertyParser.properties.add(negative)
        PropertyParser.properties.add(singleLineSqStringConstant)
        PropertyParser.properties.add(numberConstant)
    }

}
