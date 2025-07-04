package starry.auxframework.web.configuration

import starry.auxframework.context.annotation.Configuration
import starry.auxframework.context.annotation.Value

@Configuration
class ServerConfiguration(
    @Value("@{server.enabled:true}") val enabled: Boolean,
    @Value("@{server.port:\"8080\"}") val port: Int,
    @Value("@{server.hostname:\"localhost\"}") val hostname: String,
)