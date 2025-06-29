package starry.auxframework.aop

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

abstract class AfterInvocationHandler : InvocationHandler {

    abstract fun after(proxy: Any, returnValue: Any?, method: Method, args: Array<Any?>): Any?

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
        val arguments: Array<Any?> = args ?: arrayOf()
        val returnValue = method.invoke(proxy, *arguments)
        return after(proxy, returnValue, method, arguments)
    }

}