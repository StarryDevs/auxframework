package starry.auxframework.aop

import starry.auxframework.aop.annotation.Agent
import starry.auxframework.context.annotation.Configuration

@Configuration
class AgentProxyBeanProcessor : AnnotationProxyBeanPostProcessor<Agent>()
