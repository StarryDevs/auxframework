package starry.auxframework.logging.logback

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import starry.auxframework.AuxFramework
import starry.auxframework.application.util.miniMessage

class AppNameConverter : ClassicConverter() {

    override fun convert(event: ILoggingEvent) =
        "<rainbow>${AuxFramework.configurableApplicationContext?.propertyResolver?.get("app.name") ?: "aux-app"}</rainbow>".miniMessage()

}
