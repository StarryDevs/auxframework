package starry.auxframework.context.property.validation

import java.lang.RuntimeException

class ValidationException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
