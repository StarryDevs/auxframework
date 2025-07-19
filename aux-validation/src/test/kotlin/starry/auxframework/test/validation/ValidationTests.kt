package starry.auxframework.test.validation

import org.junit.jupiter.api.assertDoesNotThrow
import starry.auxframework.context.AnnotationConfigApplicationContext
import starry.auxframework.context.annotation.Value
import starry.auxframework.validation.annotation.*
import kotlin.test.Test

class ValidationTests {

    class ValidationTester {

        @Email(nullable = true)
        @Value("email@example.com", isRaw = true)
        var email: String? = null

        @Pattern("[0-9]")
        @Value("0", isRaw = true)
        var pattern: String? = null

        @IsInstance(Int::class)
        @Value("#cast(\"java.lang.Integer\", \"1\")")
        var isInstance: Any? = null

        @Size(min = 0, max = 10)
        @Value("hello", isRaw = true)
        var size: String? = null

        @Range("[0, 2]")
        @Value("2", isRaw = true)
        var range: String? = null

    }

    @Test
    fun test() {
        assertDoesNotThrow {
            val ctx = AnnotationConfigApplicationContext(ValidationTester::class, this::class.java.packageName)
            ctx.load()
        }
    }

}
