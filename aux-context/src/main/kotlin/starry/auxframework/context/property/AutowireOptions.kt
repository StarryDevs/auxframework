package starry.auxframework.context.property

import kotlin.reflect.KType

data class AutowireOptions(
    val enableValidation: Boolean = true,
    val valueType: KType? = null
)
