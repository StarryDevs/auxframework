package starry.auxframework.context.bean

class NoSuchBeanException(name: String) : RuntimeException("No bean with name '$name' found") {
    constructor(type: Class<*>) : this(type.name)

    constructor(type: Class<*>, cause: Throwable) : this(type.name) {
        initCause(cause)
    }

    constructor(name: String, cause: Throwable) : this(name) {
        initCause(cause)
    }
}
