package starry.auxframework.expression

import starry.auxframework.context.annotation.Configuration
import starry.auxframework.expression.property.PropertyExpressionParser

@Configuration
class AuxExpression {

    init {
        PropertyExpressionParser.bootstrap()
    }

}
