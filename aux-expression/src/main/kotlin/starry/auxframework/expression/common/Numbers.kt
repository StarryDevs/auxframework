package starry.auxframework.expression.common

import starry.akarui.core.chars.CharParser
import starry.akarui.core.chars.character
import starry.akarui.core.chars.symbol
import starry.akarui.core.operator.choose
import starry.akarui.core.operator.map
import starry.akarui.core.operator.optional
import starry.akarui.core.operator.repeat
import starry.akarui.core.operator.sequence
import starry.akarui.core.operator.unaryPlus
import java.math.BigInteger

val number = CharParser.sequence("Number") {
    +choose(hexNumber, octalNumber, binaryNumber, decimalNumber)
}

val decimalNumber = CharParser.sequence("DecimalNumber") {
    val integerPart = +character { it in '0' .. '9' }.repeat().map { it.joinToString("") }
    if (integerPart.length != 1 && integerPart.startsWith('0')) {
        throw makeError("Invalid number format: leading zeros are not allowed")
    }
    val decimalPart = +sequence("DecimalPart") {
        val point = +symbol(".")
        val decimal = +character { it in '0' .. '9' }.repeat().map { it.joinToString("") }
        "$point$decimal"
    }.optional().map { it.getOrElse { "" } }
    val exponentPart = +sequence("ExponentPart") {
        if (integerPart.isEmpty() && decimalPart.isEmpty()) {
            throw makeSyntaxError("Invalid number format: number part must be present")
        }
        val e = +character { it == 'e' || it == 'E' }
        val sign = +character { it == '+' || it == '-' }.optional().map { it.getOrElse { '+' } }
        val exponent = +character { it in '0'.. '9' }.repeat().map { it.joinToString("") }
        "$e$sign$exponent"
    }.optional().map { it.getOrElse { "" } }
    "$integerPart$decimalPart$exponentPart".toBigDecimal()
}

val hexNumber = CharParser.sequence("HexNumber") {
    +symbol("0")
    +character { it == 'x' || it == 'X' }
    val hex = +character { it in '0'.. '9' || it in 'a'.. 'f' || it in 'A'.. 'F' }.repeat(1).map { it.joinToString("") }
    if (hex.length!= 1 && hex.startsWith('0')) {
        throw makeError("Invalid number format: leading zeros are not allowed")
    }
    BigInteger(hex, 16).toBigDecimal()
}

val octalNumber = CharParser.sequence("OctalNumber") {
    +symbol("0")
    +character { it == 'o' || it == 'O' }
    val octal = +character { it in '0'.. '7' }.repeat(1).map { it.joinToString("") }
    if (octal.length != 1 && octal.startsWith('0')) {
        throw makeError("Invalid number format: leading zeros are not allowed")
    }
    BigInteger(octal, 8).toBigDecimal()
}

val binaryNumber = CharParser.sequence("BinaryNumber") {
    +symbol("0")
    +character { it == 'b' || it == 'B' }
    val binary = +character { it == '0' || it == '1' }.repeat(1).map { it.joinToString("") }
    if (binary.length != 1 && binary.startsWith('0')) {
        throw makeError("Invalid number format: leading zeros are not allowed")
    }
    BigInteger(binary, 2).toBigDecimal()
}
