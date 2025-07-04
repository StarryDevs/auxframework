package starry.auxframework.test.context

import org.junit.jupiter.api.Test
import starry.auxframework.context.AnnotationConfigApplicationContext
import starry.auxframework.context.annotation.Autowired
import starry.auxframework.context.annotation.Bean
import starry.auxframework.context.annotation.Value
import starry.auxframework.context.annotation.stereotype.Component
import starry.auxframework.context.bean.InitializingBean
import starry.auxframework.util.getBean
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnnotationConfigApplicationContextTest {

    @Component
    class TestComponent2 {

        @Autowired
        lateinit var dependency: TestComponent

    }

    open class TestComponentParent {

        @Value("@{user.dir}")
        lateinit var userDir: File

    }

    @Component
    class TestComponent : TestComponentParent(), InitializingBean {

        override fun afterPropertiesSet() {
            assertEquals(helloWorld, "Hello, world!")
        }

        @Value("Hello, world!")
        lateinit var helloWorld: String

        @Bean("userDir")
        fun userDir(): File {
            return userDir
        }

        @Autowired
        lateinit var dependency: TestComponent2

    }

    class TestApplication

    @Test
    fun test() {
        val ctx = AnnotationConfigApplicationContext(TestApplication::class, this::class.java.packageName)
        ctx.load()
        val bean = ctx.getBean<TestComponent>()
        assertIs<TestComponent>(bean)
        assertIs<TestComponent2>(bean.dependency)
        assertIs<File>(ctx.getBean<File>("userDir"))
    }

}