package starry.auxframework.aop

import starry.auxframework.annotation.Agent
import starry.auxframework.context.annotation.Configuration

@Configuration
class AroundProxyBeanProcessor : AnnotationProxyBeanPostProcessor<Agent>()
