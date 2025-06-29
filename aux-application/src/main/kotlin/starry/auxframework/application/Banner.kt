package starry.auxframework.application

import starry.auxframework.AuxFramework
import starry.auxframework.application.util.miniMessage
import starry.auxframework.util.readResourceAsText
import java.io.PrintStream

interface Banner {
    fun printBanner(out: PrintStream = System.out)
}

enum class Banners : Banner {
    EMPTY, DEFAULT;

    override fun printBanner(out: PrintStream) {
        if (this == EMPTY) return
        val text = this::class.readResourceAsText("banner.txt") ?: ""
        val lines = text.miniMessage().lines()
        lines.forEach(out::println)
        out.println(
            """
                <blue>Auxframework</blue>(${AuxFramework.version.version}) <gold>Java</gold>(${System.getProperty("java.version")})
            """.trimIndent().miniMessage()
        )
    }

}
