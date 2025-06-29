package starry.auxframework.aop

import arrow.core.Option

interface AroundInvocationHandler  {

    fun invoke(invocation: MethodInvocation): Option<Any?>

}
