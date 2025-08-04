package starry.auxframework.context.bean

import arrow.core.Option

interface BeanConstructor {

    fun constructBean(beanDefinition: BeanDefinition): Option<Any?>

}
