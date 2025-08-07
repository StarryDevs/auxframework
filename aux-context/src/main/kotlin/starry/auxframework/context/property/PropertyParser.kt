package starry.auxframework.context.property

import kotlinx.serialization.json.Json
import starry.adventure.parser.Parser
import starry.adventure.parser.character
import starry.adventure.parser.map
import starry.adventure.parser.operator.*
import starry.adventure.parser.symbol
import starry.adventure.parser.util.ParserSequence
import starry.adventure.parser.util.rule
import starry.adventure.parser.util.singleLineString

object PropertyParser {

    val properties: MutableSet<out Parser<out PropertyExpression>> by lazy {
        mutableSetOf(simpleExpression, callExpression, literalExpression, runningArgumentExpression, evaluateExpression)
    }

    val property: ParserSequence<PropertyExpression> = rule("property") {
        +choose(*properties.toTypedArray())
    }

    val trueLiteral by rule {
        +symbol("true").map { ConstantPropertyExpression(true) }
    }

    val falseLiteral by rule {
        +symbol("false").map { ConstantPropertyExpression(false) }
    }

    val nullLiteral by rule {
        +symbol("null").map { ConstantPropertyExpression(null) }
    }

    val stringLiteral by rule {
        +singleLineString.map { ConstantPropertyExpression(it, Json.encodeToString(it)) }
    }

    val literalExpression by rule {
        +choose(trueLiteral, falseLiteral, nullLiteral, stringLiteral)
    }

    val simpleExpressionPart by rule {
        +symbol(":")
        +property
    }

    val evaluateExpression by rule {
        +symbol("%{")
        val name = +character { it != ':' && it != '}' }.repeat().map { it.joinToString(separator = "") }
        val default = +simpleExpressionPart.optional()
        +symbol("}")
        EvaluatePropertyExpression(name, default.getOrNull())
    }

    val simpleExpression by rule {
        +symbol("@{")
        val name = +character { it != ':' && it != '}' }.repeat().map { it.joinToString(separator = "") }
        val default = +simpleExpressionPart.optional()
        +symbol("}")
        SimplePropertyExpression(name, default.getOrNull())
    }

    val callExpression by rule {
        +symbol("#")
        val name =
            +character { it.isJavaIdentifierPart() || it == '.' }.repeat().map { it.joinToString(separator = "") }
        require(!name.startsWith(".")) { "Function name cannot start with a dot" }
        val arguments = +property.list().optional().orElse { emptyList() }
        CallPropertyExpression(name, arguments)
    }

    val runningArgumentExpression by rule {
        +symbol("$")
        val index = +character { it.isDigit() }.repeat().map { it.joinToString(separator = "").toInt() }
        RunningArgumentPropertyExpression(index)
    }

}
