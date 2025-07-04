package starry.auxframework.web.handler

import arrow.core.Option
import kotlin.reflect.KParameter

interface IRequestInjectProcessor<A : Annotation> {
    fun process(parameter: KParameter, annotation: A, context: IRoutingContext): Option<*>
}