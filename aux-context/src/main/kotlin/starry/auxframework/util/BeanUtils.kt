package starry.auxframework.util

import starry.auxframework.context.bean.BeanFactory

inline fun <reified T : Any> BeanFactory.getBean() = getBean(T::class)
inline fun <reified T : Any> BeanFactory.getBeans() = getBeans(T::class)
