package starry.auxframework

import starry.auxframework.context.ConfigurableApplicationContext
import starry.auxframework.util.readResourceAsStream
import java.util.*

object AuxFramework {

    var configurableApplicationContext: ConfigurableApplicationContext? = null

    var arguments: Array<String> = arrayOf()

    private val metadata = Properties().apply {
        load(AuxFramework::class.readResourceAsStream("metadata.properties"))
    }

    val version = AuxVersion(metadata)
}

class AuxVersion(properties: Properties) {
    val version: String = properties.getProperty("version", "unknown")
    val date: String = properties.getProperty("version.date", "unknown")
}
