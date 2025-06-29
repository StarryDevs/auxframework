package starry.auxframework.aop

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

abstract class BeforeInvocationHandler : InvocationHandler {


    abstract fun before(proxy: Any, method: Method, args: Array<Any?>)

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
        val arguments: Array<Any?> = args ?: arrayOf()
        before(proxy, method, arguments)
        return method.invoke(proxy, *arguments)
    }

}