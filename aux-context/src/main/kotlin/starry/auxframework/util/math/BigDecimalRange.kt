package starry.auxframework.util.math

import starry.akarui.core.ParserState
import starry.akarui.core.chars.CharParser
import starry.akarui.core.chars.character
import starry.akarui.core.chars.symbol
import starry.akarui.core.chars.whitespace
import starry.akarui.core.operator.map
import starry.akarui.core.operator.repeat
import starry.akarui.core.operator.unaryPlus
import java.math.BigDecimal

/**
 * @param min 如果为 null，则表示没有下限
 * @param max 如果为 null，则表示没有上限
 * @param includeMin 是否包含下限
 * @param includeMax 是否包含上限
 */
class BigDecimalRange(
    val min: BigDecimal?,
    val max: BigDecimal?,
    val includeMin: Boolean = true,
    val includeMax: Boolean = true
) {

    object Parser : CharParser<BigDecimalRange> {

        override val parserName: String = "BigDecimalRangeParser"

        context(_: ParserState<Char>)
        override fun parse(): BigDecimalRange {
            +whitespace
            val includeMin = +character { it == '(' || it == '[' }.map { it == '[' }
            +whitespace
            val minCharacters = +character { it != ',' && it != ' ' }.repeat()
                .map { it.joinToString("") }
            val minValue = minCharacters.takeUnless(String::isEmpty)?.toBigDecimal()
            +whitespace
            +symbol(",")
            +whitespace
            val maxCharacters = +character { it != ')' && it != ']' && it != ' ' }.repeat()
                .map { it.joinToString("") }
            val maxValue = maxCharacters.takeUnless(String::isEmpty)?.toBigDecimal()
            +whitespace
            val includeMax = +character { it == ')' || it == ']' }.map { it == ']' }
            return BigDecimalRange(minValue, maxValue, includeMin, includeMax)
        }

    }

    init {
        require(min == null || max == null || min <= max) { "Minimum value cannot be greater than maximum value" }
    }

    operator fun contains(value: BigDecimal): Boolean {
        val minCheck = if (min == null) true else (if (includeMin) value >= min else value > min)
        val maxCheck = if (max == null) true else (if (includeMax) value <= max else value < max)
        return minCheck && maxCheck
    }

    override fun toString(): String {
        val begin = if (includeMin) "[" else "("
        val end = if (includeMax) "]" else ")"
        return "$begin${min ?: ""}, ${max ?: ""}$end"
    }

}
