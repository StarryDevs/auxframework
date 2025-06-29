package starry.auxframework.aop

import starry.auxframework.annotation.Around
import starry.auxframework.context.annotation.Configuration

@Configuration
class AroundProxyBeanProcessor : AnnotationProxyBeanPostProcessor<Around>()
