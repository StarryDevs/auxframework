package starry.auxframework.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import starry.auxframework.logging.util.miniMessage

class LoggingLevelConverter : ClassicConverter() {

    override fun convert(event: ILoggingEvent) = wrapLevelColor(event.level)

    fun wrapLevelColor(level: Level): String {
        return when (level.toInt()) {
            Level.ERROR_INT -> "<red>$level</red>"
            Level.WARN_INT -> "<gold>$level</gold>"
            Level.INFO_INT -> "<aqua>$level</aqua>"
            Level.DEBUG_INT -> "<blue>$level</blue>"
            else -> "$level"
        }.miniMessage()
    }

}
