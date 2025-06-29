package starry.auxframework.io

import starry.auxframework.util.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo
import kotlin.io.path.toPath
import kotlin.io.path.walk


class ResourceResolver(private val classLoaders: Set<ClassLoader>, private val basePackage: String) {

    private val logger = getLogger()

    fun <R> scan(mapper: (Resource) -> R?): Set<R> {
        val path = basePackage.replace(".", "/")
        val result = mutableSetOf<R>()
        scan(path, result, mapper)
        return result
    }

    private fun <R> scan(path: String, result: MutableSet<R>, mapper: (Resource) -> R?) {
        for (classLoader in classLoaders) {
            val resources = classLoader.getResources(path)
            for (url in resources) {
                val uri = decodeUri(url.toURI()).removeTail(SEPARATOR_LEFT, SEPARATOR_RIGHT)
                val base = uri.substring(0, uri.length - path.length)
                    .removePrefix(FILE_PREFIX)
                if (uri.startsWith(JAR_PREFIX))
                    scan(true, base, Paths.fromJar(path, url.toURI()), result, mapper)
                else
                    scan(false, base, url.toURI().toPath(), result, mapper)
            }
        }

    }

    private fun <R> scan(
        isJar: Boolean,
        base: String,
        rootPath: Path,
        result: MutableSet<R>,
        mapper: (Resource) -> R?
    ) {
        val baseDir = base.removeTail(SEPARATOR_LEFT, SEPARATOR_RIGHT)
        rootPath.walk()
            .filter(Files::isRegularFile)
            .forEach { file ->
                val resource = if (isJar) {
                    Resource(baseDir.replace("\\", "/"), file.toString().replace("\\", "/").removeHead(SEPARATOR_LEFT))
                } else {
                    val path = file.toString()
                    val relativeDir = baseDir.removeHead(SEPARATOR_LEFT, SEPARATOR_RIGHT)
                    val name = file.relativeTo(Path.of(relativeDir)).pathString.replace("\\", "/").removeHead(SEPARATOR_LEFT)
                    Resource("$FILE_PREFIX$path", name)
                }
                mapper(resource)?.let(result::add)
            }
    }

    companion object {

        const val FILE_PREFIX = "file:"
        const val JAR_PREFIX = "jar:"
        const val SEPARATOR_LEFT = "/"
        const val SEPARATOR_RIGHT = "\\"

    }

}

