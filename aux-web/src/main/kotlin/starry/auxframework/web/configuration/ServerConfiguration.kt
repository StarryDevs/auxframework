package starry.auxframework.web.configuration

import starry.auxframework.context.annotation.Configuration
import starry.auxframework.context.annotation.Property
import starry.auxframework.context.annotation.Value

@Configuration
class ServerConfiguration(
    @param:Property("server.enabled") @param:Value("@{server.enabled:true}") var enabled: Boolean,
    @param:Property("server.port") @param:Value("@{server.port:\"8080\"}") var port: Int,
    @param:Property("server.hostname") @param:Value("@{server.hostname:\"localhost\"}") var hostname: String,
)
