package starry.auxframework.util

fun loadPropertiesFromMap(map: Map<String, Any?>, saved: MutableSet<Pair<String, String>>, prefix: String = "") {
    for ((key, value) in map) {
        val newPrefix = if (prefix.isEmpty()) key else "$prefix.$key"
        when (value) {
            is Map<*, *> -> loadPropertiesFromMap(value.mapKeys { it.key.toString() }.toMap(), saved, newPrefix)
            is Collection<*> -> loadPropertiesFromMap(
                value.withIndex().associateBy { it.index.toString() },
                saved,
                newPrefix
            )

            else -> saved.add(newPrefix to value.toString())
        }
    }
}
