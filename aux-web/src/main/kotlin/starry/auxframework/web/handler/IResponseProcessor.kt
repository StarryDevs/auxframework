package starry.auxframework.web.handler

interface IResponseProcessor<T> {
    suspend fun process(value: T, context: IRoutingContext)
}
