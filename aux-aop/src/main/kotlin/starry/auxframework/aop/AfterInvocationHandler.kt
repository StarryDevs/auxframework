package starry.auxframework.aop

interface AfterInvocationHandler {

    fun after(invocation: MethodInvocation, returnValue: Any?): Any?

}
