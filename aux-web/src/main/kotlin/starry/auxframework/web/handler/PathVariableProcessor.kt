package starry.auxframework.web.handler

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import starry.auxframework.context.annotation.stereotype.Component
import starry.auxframework.web.annotation.PathVariable
import kotlin.reflect.KParameter

@Component
class PathVariableProcessor : IRequestInjectProcessor<PathVariable> {

    override fun process(parameter: KParameter, annotation: PathVariable, context: IRoutingContext): Option<*> {
        val name = annotation.name.takeUnless(String::isEmpty) ?: parameter.name ?: return None
        return Some(context.call.pathParameters[name])
    }

}