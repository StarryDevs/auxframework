package starry.auxframework.aop

interface BeforeInvocationHandler {

    fun before(invocation: MethodInvocation)

}