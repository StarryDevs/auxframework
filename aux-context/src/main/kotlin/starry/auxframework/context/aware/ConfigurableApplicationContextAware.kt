package starry.auxframework.context.aware

import starry.auxframework.context.ConfigurableApplicationContext

interface ConfigurableApplicationContextAware {

    fun setConfigurableApplicationContext(context: ConfigurableApplicationContext)

}