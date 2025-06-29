package starry.auxframework.context.aware

import starry.auxframework.context.bean.BeanFactory

interface BeanFactoryAware {

    fun setBeanFactory(beanFactory: BeanFactory)

}