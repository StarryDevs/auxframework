package starry.auxframework.expression.common

import starry.adventure.parser.buffer
import starry.adventure.parser.symbol
import starry.adventure.parser.util.rule

val singleLineSqString by rule {
    +symbol("'")
    val content = buildString {
        while (buffer.hasRemaining()) {
            val c = buffer.get()
            when (c) {
                '\'' -> {
                    // 结束
                    break
                }
                '\\' -> {
                    if (!buffer.hasRemaining()) error("Unterminated escape")
                    val esc = buffer.get()
                    append(
                        when (esc) {
                            '\'', '\\', '/' -> esc
                            'b' -> '\b'
                            'f' -> '\u000C'
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            'v' -> '\u000B'
                            '0' -> '\u0000'
                            'x' -> {
                                val hex = CharArray(2) {
                                    if (!buffer.hasRemaining()) error("Invalid \\x escape")
                                    buffer.get()
                                }
                                hex.concatToString().toInt(16).toChar()
                            }
                            'u' -> {
                                val hex = CharArray(4) {
                                    if (!buffer.hasRemaining()) error("Invalid \\u escape")
                                    buffer.get()
                                }
                                hex.concatToString().toInt(16).toChar()
                            }
                            else -> error("Invalid escape: \\$esc")
                        }
                    )
                }
                '\n', '\r' -> error("Single-quoted string cannot contain line breaks")
                else -> append(c)
            }
        }
    }
    content
}
