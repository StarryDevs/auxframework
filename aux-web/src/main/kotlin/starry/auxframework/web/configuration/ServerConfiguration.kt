package starry.auxframework.web.configuration

import starry.auxframework.context.annotation.Configuration
import starry.auxframework.context.annotation.Property
import starry.auxframework.context.annotation.Value

@Configuration
class ServerConfiguration(
    @Property("server.enabled") @Value("@{server.enabled:true}") var enabled: Boolean,
    @Property("server.port") @Value("@{server.port:\"8080\"}") var port: Int,
    @Property("server.hostname") @Value("@{server.hostname:\"localhost\"}") var hostname: String,
)
