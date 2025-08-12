package starry.auxframework.context.property

import starry.akarui.core.chars.CharParser
import starry.akarui.core.chars.character
import starry.akarui.core.chars.symbol
import starry.akarui.core.chars.whitespace
import starry.akarui.core.operator.choose
import starry.akarui.core.operator.list
import starry.akarui.core.operator.map
import starry.akarui.core.operator.optional
import starry.akarui.core.operator.orElse
import starry.akarui.core.operator.repeat
import starry.akarui.core.operator.unaryPlus
import starry.akarui.tokenizer.SingleLineStringParser

object PropertyParser {

    val properties: MutableSet<CharParser<out PropertyExpression>> by lazy {
        mutableSetOf(simpleExpression, callExpression, literalExpression, programArgumentExpression, evaluateExpression)
    }

    val property: CharParser<PropertyExpression> = CharParser.sequence("Property") {
        +choose(*properties.toTypedArray())
    }

    val trueLiteral = CharParser.sequence("TrueLiteral") {
        +symbol("true").map { ConstantPropertyExpression(true) }
    }

    val falseLiteral = CharParser.sequence("FalseLiteral") {
        +symbol("false").map { ConstantPropertyExpression(false) }
    }

    val nullLiteral = CharParser.sequence("NullLiteral") {
        +symbol("null").map { ConstantPropertyExpression(null) }
    }

    val stringLiteral = CharParser.sequence("StringLiteral") {
        +choose(*enumValues<SingleLineStringParser>()).map { ConstantPropertyExpression(it.value, it.raw) }
    }

    val literalExpression = CharParser.sequence("Literal") {
        +choose(trueLiteral, falseLiteral, nullLiteral, stringLiteral)
    }

    val evaluateExpression = CharParser.sequence("EvaluateExpression") {
        +symbol("%{")
        val name = +character { it != ':' && it != '}' }.repeat().map { it.joinToString(separator = "") }
        val default = +simpleExpressionPart.optional()
        +symbol("}")
        EvaluatePropertyExpression(name, default.getOrNull())
    }

    val simpleExpressionPart = CharParser.sequence("SimpleExpressionPart") {
        +symbol(":")
        +property
    }

    val simpleExpression = CharParser.sequence("SimpleExpression") {
        +symbol("@{")
        val name = +character { it != ':' && it != '}' }.repeat().map { it.joinToString(separator = "") }
        val default = +simpleExpressionPart.optional()
        +symbol("}")
        SimplePropertyExpression(name, default.getOrNull())
    }

    val callExpression = CharParser.sequence("CallExpression") {
        +symbol("#")
        val name =
            +character { it.isJavaIdentifierPart() || it == '.' }.repeat().map { it.joinToString(separator = "") }
        require(!name.startsWith(".")) { "Function name cannot start with a dot" }
        val arguments = +property.list(
            symbol("("), symbol(")"), symbol(","), whitespace
        ).optional().orElse { emptyList() }
        CallPropertyExpression(name, arguments)
    }

    val programArgumentExpression = CharParser.sequence("ProgramArgumentExpression") {
        +symbol("$")
        val index = +character { it.isDigit() }.repeat().map { it.joinToString(separator = "").toInt() }
        ProgramArgumentPropertyExpression(index)
    }

}
