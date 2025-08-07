package starry.auxframework.util

import starry.auxframework.context.bean.BeanFactory

inline fun <reified T : Any> BeanFactory.getBean(symbol: String? = null) = getBean(T::class, symbol)
inline fun <reified T : Any> BeanFactory.getBeans(symbol: String? = null) = getBeans(T::class, symbol)
