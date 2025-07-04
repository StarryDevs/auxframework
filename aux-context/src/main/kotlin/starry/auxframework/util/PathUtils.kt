package starry.auxframework.util

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path

object Paths {

    fun fromJar(basePackagePath: String, jar: URI): Path =
        runCatching { FileSystems.getFileSystem(jar) }
            .getOrElse { FileSystems.newFileSystem(jar, mapOf<String, Any?>()) }
            .getPath(basePackagePath)

}
