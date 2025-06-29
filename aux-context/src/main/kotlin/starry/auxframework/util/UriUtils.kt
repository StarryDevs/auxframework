package starry.auxframework.util

import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

fun decodeUri(uri: String): String =
    URLDecoder.decode(uri, Charsets.UTF_8)


fun decodeUri(uri: URI) = decodeUri(uri.toString())

fun encodeUri(uri: String): String = URLEncoder.encode(uri, Charsets.UTF_8)
