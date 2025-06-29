package starry.auxframework.aop

import java.lang.reflect.Method


class MethodInvocation(val proxy: Any, val instance: Any, val method: Method, val args: Array<out Any?>) {

    fun process(): Any? = method.invoke(instance, *args)

}
