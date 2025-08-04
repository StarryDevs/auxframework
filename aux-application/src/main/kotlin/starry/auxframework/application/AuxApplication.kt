package starry.auxframework.application

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import starry.auxframework.AuxFramework
import starry.auxframework.util.loadPropertiesFromMap
import starry.auxframework.context.AnnotationConfigApplicationContext
import starry.auxframework.context.ConfigurableApplicationContext
import starry.auxframework.util.getLogger
import starry.auxframework.util.readResourceAsStream
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.reflect.KClass
import kotlin.time.measureTime

data class AuxApplicationBuilder(
    val banner: Banner = Banners.DEFAULT,
    val applicationClass: KClass<*>? = null,
    val basePackages: Set<String> = emptySet(),
    val classLoaders: Set<ClassLoader> = emptySet(),
    val context: AuxApplicationBuilder.() -> ConfigurableApplicationContext = {
        AnnotationConfigApplicationContext(
            this.applicationClass,
            *this.basePackages.toTypedArray(),
            classLoaders = this.classLoaders
        )
    }
) {
    fun banner(banner: Banner) = copy(banner = banner)
    fun applicationClass(applicationClass: KClass<*>) = copy(applicationClass = applicationClass)
    fun basePackages(vararg packages: String) = copy(basePackages = this.basePackages + packages)
    fun classLoaders(vararg loaders: ClassLoader) = copy(classLoaders = this.classLoaders + loaders)

    fun build() = AuxApplication(this)

}

class AuxApplication(private val builder: AuxApplicationBuilder = AuxApplicationBuilder()) {

    val logger = getLogger()

    fun start(args: Array<String>): ConfigurableApplicationContext {
        logger.info("Starting AuxApplication with arguments: ${args.joinToString(", ")}")
        AuxFramework.arguments = args
        builder.banner.printBanner()
        val context = builder.context(builder)
        AuxFramework.configurableApplicationContext = context
        val time = measureTime {
            loadConfig(context.propertyResolver)
            context.load()
        }
        Runtime.getRuntime().addShutdownHook(Thread(::shutdownHook, "AuxApplication Shutdown Hook"))
        logger.info("Application context loaded in ${time.inWholeMilliseconds}ms.")
        return context
    }

    private fun shutdownHook() {
        logger.info("Shutting down application context...")
        AuxFramework.configurableApplicationContext?.close()
        logger.info("Application context shut down successfully.")
    }

    private fun loadConfig(map: MutableMap<String, String>) {
        val fileAppHocon = File("./application.conf")
        if (fileAppHocon.isFile) {
            loadHoconConfig(map, ConfigFactory.parseFile(fileAppHocon))
        } else {
            logger.debug("No application.conf found in current directory.")
        }
        val fileAppProperties = File("./application.properties")
        if (fileAppProperties.isFile) {
            loadPropertiesConfig(map, fileAppProperties.inputStream())
        } else {
            logger.debug("No application.properties found in current directory.")
        }
        if (AuxApplication::class.readResourceAsStream("/application.conf")?.apply(InputStream::close) == null) {
            logger.debug("No application.conf found in classpath.")
        } else {
            loadHoconConfig(map, ConfigFactory.parseResources("application.conf"))
        }
        val classpathAppProperties = AuxApplication::class.readResourceAsStream("/application.properties")
        if (classpathAppProperties != null) {
            loadPropertiesConfig(map, classpathAppProperties)
        } else {
            logger.debug("No application.properties found in classpath.")
        }
    }

    private fun loadHoconConfig(
        map: MutableMap<String, String>,
        config: Config,
    ) {
        val properties = mutableSetOf<Pair<String, String>>()
        loadPropertiesFromMap(config.resolve().root().unwrapped(), properties)
        for ((key, value) in properties) {
            if (key in map) continue
            map[key] = value
        }
    }

    private fun loadPropertiesConfig(
        map: MutableMap<String, String>,
        data: InputStream,
    ) {
        val properties = data.use {
            Properties().apply { load(it) }
        }
        for (key in properties.stringPropertyNames()) {
            if (key in map) continue
            val value = properties.getProperty(key)
            map[key] = value
        }

    }

}

fun main(args: Array<String>) {
    AuxApplication().start(args)
}
