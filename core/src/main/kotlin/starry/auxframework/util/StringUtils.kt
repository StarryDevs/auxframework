package starry.auxframework.util

fun String.removeHead(vararg texts: String): String {
    for (text in texts) {
        if (startsWith(text)) return removePrefix(text)
    }
    return this
}

fun String.removeTail(vararg texts: String): String {
    for (text in texts) {
        if (endsWith(text)) return removeSuffix(text)
    }
    return this
}
