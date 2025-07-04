package starry.auxframework.web.annotation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@Repeatable
annotation class RequestMapping(val path: String, val methods: Array<String> = ["GET", "POST"])