package starry.auxframework.test.expression

import org.junit.jupiter.api.assertDoesNotThrow
import starry.auxframework.context.AnnotationConfigApplicationContext
import starry.auxframework.context.annotation.Value
import starry.auxframework.context.bean.InitializingBean
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionTests {

    class ExpressionTester : InitializingBean {

        @Value("-1")
        var negative: Int = 0

        @Value("+'1'")
        var positive: Int = 0

        @Value("'Hello, world!'")
        lateinit var text: String

        override fun afterPropertiesSet() {
            assertEquals(negative, -1)
            assertEquals(positive, 1)
            assertEquals(text, "Hello, world!")
        }

    }

    @Test
    fun test() {
        assertDoesNotThrow {
            AnnotationConfigApplicationContext(
                ExpressionTester::class,
                this::class.java.packageName
            ).load()
        }
    }

}
