package starry.auxframework.web.handler

import starry.auxframework.context.annotation.stereotype.Component
import starry.auxframework.web.model.IModel

@Component
class ModelResponseProcessor : IResponseProcessor<IModel> {

    override suspend fun process(value: IModel, context: IRoutingContext) {
        value.render(context)
    }

}
