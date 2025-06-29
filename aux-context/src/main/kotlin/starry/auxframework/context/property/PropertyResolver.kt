package starry.auxframework.context.property

import starry.adventure.parser.parse
import starry.auxframework.util.IBootstrap
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.CharBuffer
import java.nio.file.Path
import java.util.*
import kotlin.io.path.toPath
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.reflect.KClass
import kotlin.reflect.cast

private fun Properties.toEnvMap() = buildMap {
    putAll(System.getenv())
    val properties = System.getProperties()
    for (name in properties.stringPropertyNames()) {
        put(name, properties.getProperty(name))
    }
    for (name in stringPropertyNames()) {
        put(name, getProperty(name))
    }
}

class PropertyResolver(properties: Map<String, String>): MutableMap<String, String> by properties.toMutableMap() {

    companion object {
        @JvmField
        val CONVERTERS = mutableMapOf<KClass<*>, PropertyResolver.(value: Any?) -> Any?>()

        @Suppress("UNCHECKED_CAST")
        @JvmField
        val FUNCTIONS = mutableMapOf<String, PropertyResolver.(arguments: List<PropertyExpression>) -> Any?>()

        fun <T> addFunction(name: String, function: PropertyResolver.(arguments: List<PropertyExpression>) -> T): PropertyResolver.(List<PropertyExpression>) -> T {
            if (FUNCTIONS.containsKey(name)) {
                throw IllegalArgumentException("Function '$name' is already defined")
            }
            FUNCTIONS[name] = function
            return function
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> addConverter(type: KClass<T>, converter: PropertyResolver.(value: Any?) -> T?): PropertyResolver.(Any?) -> T? {
            CONVERTERS[type] = CONVERTERS[type]?.let { existingConverter ->
                { value -> converter(value) ?: existingConverter(value) }
            } ?: converter
            return CONVERTERS[type] as PropertyResolver.(Any?) -> T?
        }

        inline fun <reified T: Any> addConverter(noinline converter: PropertyResolver.(value: Any?) -> T?) =
            addConverter(T::class, converter)

        init {
            Functions.bootstrap()
            Converters.bootstrap()
        }

    }

    object Converters : IBootstrap {

        val toList = addConverter {
            when (it) {
                is Array<*> -> it.toList()
                is Iterable<*> -> it.toList()
                is Iterator<*> -> it.asSequence().toList()
                else -> listOf(it)
            }
        }

        val toFile = addConverter {
            when (it) {
                is File -> it
                is Path -> it.toFile()
                else -> resolve<String>(it)?.let { path -> File(path) }
            }
        }

        val toPath = addConverter {
            when (it) {
                is URI -> it.toPath()
                is URL -> it.toURI().toPath()
                is Path -> it
                is File -> it.toPath()
                else -> resolve<File>(it)?.toPath()
            }
        }

        val toString = addConverter { it?.toString() }

        val toBoolean = addConverter {
            when (it) {
                null -> false
                is Boolean -> it
                is String -> it.isNotEmpty() && it != "false"
                else -> resolve<Float>(it) != 0F
            }
        }

        val toBigInt = addConverter { resolve<String>(it)?.toBigInteger() }
        val toBigDecimal = addConverter { resolve<String>(it)?.toBigDecimal() }
        val toShort = addConverter { resolve<String>(it)?.toShort() }
        val toByte = addConverter { resolve<String>(it)?.toByte() }
        val toInt = addConverter { resolve<String>(it)?.toInt() }
        val toLong = addConverter { resolve<String>(it)?.toLong() }
        val toDouble = addConverter { resolve<String>(it)?.toDouble() }
        val toFloat = addConverter { resolve<String>(it)?.toFloat() }
    }

    object Functions : IBootstrap {
        val join = addFunction("join") { args ->
            require(args.isNotEmpty()) { "Function 'join' requires at least one argument" }
            val separator = resolve<String>(args.getOrNull(0)) ?: ""
            args.drop(1).joinToString(separator) { resolve<String>(it) ?: "" }
        }

        val random = addFunction("random") {
            require(it.isEmpty() || it.size == 2 || it.size == 3) { "Function 'random' requires zero, two, or three arguments" }
            if (it.isEmpty()) Math.random()
            else {
                val min = resolve<Long>(it[0]) ?: Long.MIN_VALUE
                val max = resolve<Long>(it.getOrNull(1)) ?: Long.MAX_VALUE
                val seed = it.getOrNull(2)?.let { seed -> resolve<Long>(seed) } ?: System.currentTimeMillis()
                Random(seed).nextLong(min..max).toDouble()
            }
        }

        val cast = addFunction("cast") {
            require(it.size == 2) { "Function 'cast' requires exactly two arguments" }
            val typeName = resolve<String>(it[0]) ?: throw IllegalArgumentException("Type name must be a non-null string")
            val value = it[1].resolve(this)
            val type = Functions.javaClass.classLoader.loadClass(typeName) ?: throw IllegalArgumentException("Type '$typeName' not found")
            resolve(type.kotlin, value)
        }

        val toBoolean = addFunction("toBoolean") {
            require(it.size == 1) { "Function 'toBoolean' requires exactly one argument" }
            resolve<Boolean>(it[0]) ?: false
        }

        val not = addFunction("not") {
            require(it.size == 1) { "Function 'not' requires exactly one argument" }
            !toBoolean(it)
        }

        val all = addFunction("all") { args ->
            args.all { toBoolean(listOf(it)) }
        }

        val any = addFunction("any") { args ->
            args.any { toBoolean(listOf(it)) }
        }

        val isNull = addFunction("isNull") {
            require(it.size == 1) { "Function 'isNull' requires exactly one argument" }
            it[0].resolve(this) == null
        }

        val `if` = addFunction("if") {
            require(it.size == 2 || it.size == 3) { "Function 'if' requires two or three arguments" }
            val condition = resolve<Boolean>(it[0])
            if (condition == true) {
                it[1].resolve(this)
            } else {
                if (it.size == 3) it[2].resolve(this) else null
            }
        }

    }

    constructor(properties: Properties) : this(properties.toEnvMap())

    fun <T : Any> resolve(type: KClass<T>, value: Any?): T? {
        return if (type.isInstance(value)) type.cast(value)
        else type.cast(CONVERTERS[type]?.invoke(this, value))
    }

    fun call(name: String, arguments: List<PropertyExpression>): Any? {
        val function = FUNCTIONS[name] ?: throw IllegalArgumentException("Function '$name' is not defined.")
        return function(arguments)
    }

    fun parse(text: String) = try {
        CharBuffer.wrap(text).parse(PropertyParser.property)
    } catch (_: Throwable) {
        ConstantPropertyExpression(text, text)
    }

}

inline fun <reified T : Any> PropertyResolver.resolve(value: Any?) =
    resolve(T::class, value)


inline fun <reified T : Any> PropertyResolver.resolve(expression: PropertyExpression) =
    resolve(T::class, expression.resolve(this))
