package starry.auxframework.web.configuration

import starry.auxframework.context.annotation.Configuration
import starry.auxframework.context.annotation.Value

@Configuration
class ServerConfiguration(
    @Value("@{server.enabled:true}") var enabled: Boolean,
    @Value("@{server.port:\"8080\"}") var port: Int,
    @Value("@{server.hostname:\"localhost\"}") var hostname: String,
)
