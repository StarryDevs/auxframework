package starry.auxframework.context.property

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.serializer

class PropertyHelper(private val propertyResolver: PropertyResolver) {

    companion object {

        fun loadPropertiesFromMap(map: Map<String, Any?>, saved: MutableSet<Pair<String, String>>, prefix: String = "") {
            for ((key, value) in map) {
                val newPrefix = if (prefix.isEmpty()) key else "$prefix.$key"
                when (value) {
                    is Map<*, *> -> loadPropertiesFromMap(
                        value.mapKeys { it.key.toString() },
                        saved,
                        newPrefix
                    )
                    is Iterable<*> -> loadPropertiesFromMap(
                        value.withIndex().associate { it.index.toString() to it.value },
                        saved,
                        newPrefix
                    )
                    else -> saved.add(newPrefix to value.toString())
                }
            }
        }


    }

    operator fun contains(prefix: String) = getAll(prefix).isNotEmpty()

    fun getAll(prefix: String): Map<String, String> {
        return propertyResolver.entries
            .filter { it.key.startsWith("$prefix.") }
            .associate { it.key.removePrefix("$prefix.") to it.value }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun <T> get(deserializer: DeserializationStrategy<T>, prefix: String): T =
        Properties.decodeFromStringMap(deserializer, getAll(prefix))

    @OptIn(ExperimentalSerializationApi::class)
    inline operator fun <reified T> get(prefix: String) =
        get(serializer<T>(), prefix)

}

