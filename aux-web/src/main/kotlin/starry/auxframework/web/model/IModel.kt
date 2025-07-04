package starry.auxframework.web.model

import starry.auxframework.web.handler.IRoutingContext

interface IModel {
    suspend fun render(response: IRoutingContext)
}