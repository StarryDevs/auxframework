package starry.auxframework.web.handler

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import starry.adventure.core.util.IWrapped
import starry.auxframework.context.annotation.stereotype.Component
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.reflect.full.createType

@Component
class BaseResponseProcessor : IResponseProcessor<Any?> {

    @OptIn(ExperimentalSerializationApi::class)
    companion object {

        @JvmField
        val SERIALIZERS =
            mutableMapOf<ContentType, (value: Any, charset: Charset, outputStream: OutputStream) -> Unit>()

        @JvmField
        val JSON = Json {
            prettyPrint = true
            prettyPrintIndent = " ".repeat(4)
        }

        init {
            SERIALIZERS[ContentType.Application.Json] = { value, charset, outputStream ->
                JSON.encodeToStream(serializer(value::class.createType()), value, outputStream)
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun process(value: Any?, context: IRoutingContext) = context.run {
        if (value == null) return
        when (value) {
            is String -> call.respondText(value)
            is ByteArray -> call.respondBytes(value)
            is InputStream -> call.respondOutputStream { value.copyTo(this) }
            is IWrapped<*> -> {
                val contentType = call.request.contentType()
                call.respondOutputStream(contentType) {
                    val charset = call.request.contentCharset() ?: Charsets.UTF_8
                    val unwrap = (value as IWrapped<Any?>).unwrap()
                    if (unwrap != null) (SERIALIZERS[contentType]
                        ?: SERIALIZERS[ContentType.Application.Json])
                        ?.invoke(unwrap, charset, this)
                }
            }
        }
    }
}