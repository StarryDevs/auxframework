package starry.auxframework.web.annotation

import starry.auxframework.context.annotation.stereotype.Controller
import java.lang.annotation.Inherited

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Controller
@Inherited
annotation class RestController(val path: String = "")
