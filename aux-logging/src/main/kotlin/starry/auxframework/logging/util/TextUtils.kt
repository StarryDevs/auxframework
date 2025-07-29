package starry.auxframework.logging.util

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer

fun String.miniMessage(): String {
    val miniMessage = MiniMessage.miniMessage().deserialize(this)
    return ANSIComponentSerializer.ansi().serialize(miniMessage)
}
