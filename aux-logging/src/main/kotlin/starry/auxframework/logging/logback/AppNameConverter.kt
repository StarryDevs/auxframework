package starry.auxframework.logging.logback

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import starry.auxframework.AuxFramework
import starry.auxframework.logging.util.miniMessage

class AppNameConverter : ClassicConverter() {

    override fun convert(event: ILoggingEvent) =
        "<aqua>${AuxFramework.configurableApplicationContext?.propertyResolver?.get("app.name") ?: "aux-app"}</aqua>".miniMessage()

}
