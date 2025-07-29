package starry.auxframework.context

import starry.auxframework.AuxFramework
import starry.auxframework.context.annotation.*
import starry.auxframework.context.annotation.stereotype.Indexed
import starry.auxframework.context.aware.BeanFactoryAware
import starry.auxframework.context.aware.ConfigurableApplicationContextAware
import starry.auxframework.context.bean.ApplicationListener
import starry.auxframework.context.bean.BeanDefinition
import starry.auxframework.context.bean.BeanPostProcessor
import starry.auxframework.context.bean.InitializingBean
import starry.auxframework.context.property.AutowireOptions
import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.io.ResourceResolver
import starry.auxframework.util.findAnnotation
import starry.auxframework.util.getBeans
import java.lang.reflect.Array
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmName

class DependencyList : Iterable<BeanDefinition> {

    private val list: MutableList<BeanDefinition> = mutableListOf()

    operator fun contains(beanDefinition: BeanDefinition) = beanDefinition in list

    fun add(beanDefinition: BeanDefinition) {
        if (beanDefinition in list) return
        list.add(beanDefinition)
    }

    override fun iterator() = list.iterator()

}

open class AnnotationConfigApplicationContext(
    protected val applicationClass: KClass<*>? = null,
    protected vararg val basePackages: String,
    protected val classLoaders: Set<ClassLoader> = setOf(Thread.currentThread().contextClassLoader),
) :
    ConfigurableApplicationContext() {

    protected val beans = mutableMapOf<String, BeanDefinition>()

    override val propertyResolver = PropertyResolver(this, Properties())

    protected val beanPostProcessors = mutableListOf<BeanPostProcessor>()

    protected fun collectImports(
        annotated: KClass<*>,
        classes: MutableSet<KClass<*>>,
        stack: MutableList<KClass<*>> = mutableListOf()
    ) {
        stack += annotated
        val import = findAnnotation(annotated, Import::class) ?: return
        import.classes.forEach { collectImports(it, classes, stack) }
    }

    protected var loaded: Boolean = false

    override fun load() {
        if (loaded) return
        loaded = true
        registerSingleton(this)
        val beanDefinitions = mutableMapOf<String, BeanDefinition>()
        val beanClasses = mutableSetOf<KClass<*>>()
        if (applicationClass != null) beanClasses += applicationClass
        val classLoaders = buildSet<ClassLoader> {
            add(AuxFramework::class.java.classLoader)
            addAll(this@AnnotationConfigApplicationContext.classLoaders)
        }
        val basePackages = buildSet {
            add(AuxFramework::class.java.packageName)
            addAll(this@AnnotationConfigApplicationContext.basePackages)
        }
        for (basePackage in basePackages) {
            beanClasses += ResourceResolver(classLoaders, basePackage).scan {
                if (!it.name.endsWith(".class")) null
                else it.name.replace("/", ".").removeSuffix(".class")
            }.mapNotNull {
                runCatching { Class.forName(it).kotlin }.getOrNull()
            }.filter {
                !it.java.isAnnotation
            }.filter { findAnnotation(it, Indexed::class) != null }
        }
        val importClasses = mutableSetOf<KClass<*>>()
        beanClasses.forEach { collectImports(it, importClasses) }
        beanClasses += importClasses
        for (beanClass in beanClasses) {
            val baseBeanAnnotation = beanClass.findAnnotation<Bean>()
            val beanDefinition = BeanDefinition(
                baseBeanAnnotation?.name?.takeUnless(String::isEmpty) ?: beanClass.jvmName,
                beanClass,
                constructor = beanClass.constructors.firstOrNull(),
                initMethodName = baseBeanAnnotation?.initMethod?.takeUnless(String::isEmpty),
                destroyMethodName = baseBeanAnnotation?.destroyMethod?.takeUnless(String::isEmpty),
                initMethod = beanClass.memberFunctions.firstOrNull { it.hasAnnotation<PostConstruct>() },
                destroyMethod = beanClass.memberFunctions.firstOrNull { it.hasAnnotation<PreDestroy>() }
            )
            beanDefinitions[beanDefinition.name] = beanDefinition
            for (member in beanClass.members) {
                val beanAnnotation = member.findAnnotation<Bean>() ?: continue
                val memberBeanName = beanAnnotation.name.ifEmpty { "${beanClass.jvmName}::${member.name}" }
                val beanClassType = member.returnType
                val memberBeanDefinition = BeanDefinition(
                    memberBeanName,
                    beanClassType.classifier as KClass<*>,
                    constructor = member,
                    initMethodName = beanAnnotation.initMethod.takeUnless(String::isEmpty),
                    destroyMethodName = beanAnnotation.destroyMethod.takeUnless(String::isEmpty)
                )
                beanDefinitions[memberBeanName] = memberBeanDefinition
            }
        }
        beans.putAll(beanDefinitions)
        beanDefinitions.clear()
        val dependencyList = DependencyList()
        beans.values.sortedByDescending { it.beanClass.isSubclassOf(BeanPostProcessor::class) }.forEach {
            collectDependencies(dependencyList, it)
        }
        dependencyList.forEachIndexed { index, beanDefinition ->
            beanDefinition.order = index
        }
        val configurations = mutableSetOf<BeanDefinition>()
        val nonConfigurations = mutableSetOf<BeanDefinition>()
        for (beanDefinition in dependencyList) {
            if (beanDefinition.beanClass.hasAnnotation<Configuration>()) configurations += beanDefinition
            else nonConfigurations += beanDefinition
        }
        construct(configurations)
        construct(nonConfigurations)
        getBeans<ApplicationListener>().forEach(ApplicationListener::finishLoading)
    }

    protected fun construct(beans: Set<BeanDefinition>) {
        beans.sortedBy { it.order }.forEach(::construct)
    }

    private fun construct(beanDefinition: BeanDefinition): Any? {
        if (beanDefinition.constructed) return beanDefinition.instanceObject
        val constructor = beanDefinition.constructor ?: return null
        val enableValidation = (constructor.findAnnotation<EnableValidation>()?.enabled)
            ?: (beanDefinition.beanClass.findAnnotation<EnableValidation>()?.enabled == true)
        val arguments = mutableMapOf<KParameter, Any?>()
        for (parameter in constructor.parameters) {
            val type = parameter.type.classifier as KClass<*>
            val annotations = parameter.annotations
            val autowireOptions = AutowireOptions(
                enableValidation = enableValidation
            )
            val value = autowire(type, annotations, autowireOptions)
            if (value != null) arguments[parameter] = value
            else if (!parameter.isOptional) arguments[parameter] = null
        }
        val instance = constructor.callBy(arguments)
        beanDefinition.instanceObject = instance
        beanDefinition.getInitMethod()?.call(instance)
        callAwares(beanDefinition.instanceObject)
        beanDefinition.instanceObject = beanPostProcessors.fold(beanDefinition.instanceObject) { it, processor ->
            processor.postProcessBeforeInitialization(it, beanDefinition.name, this)
        }
        if (beanDefinition.instanceObject is BeanPostProcessor) {
            beanPostProcessors += beanDefinition.instanceObject as BeanPostProcessor
        }
        beanDefinition.constructed = true
        autowire(beanDefinition)
        return beanDefinition.instanceObject
    }

    protected fun callAwares(obj: Any?) {
        if (obj == null) return
        if (obj is BeanFactoryAware) obj.setBeanFactory(this)
        if (obj is ConfigurableApplicationContextAware) obj.setConfigurableApplicationContext(this)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun autowire(beanDefinition: BeanDefinition) = beanDefinition.also {
        if (beanDefinition.propertySet) return@also
        val instance = beanPostProcessors.fold(beanDefinition.instanceObject) { it, processor ->
            processor.postProcessOnSetProperty(it, beanDefinition.name, this)
        }
        val enableValidation = beanDefinition.beanClass.findAnnotation<EnableValidation>()?.enabled == true
        for (member in beanDefinition.beanClass.memberProperties) {
            if (member !is KMutableProperty<*>) continue
            if (!member.hasAnnotation<Autowired>() && !member.hasAnnotation<Value>()) continue
            val type = member.returnType.classifier as? KClass<*> ?: continue
            val annotations = member.annotations
            val value = try {
                val options = AutowireOptions(enableValidation = enableValidation)
                autowire(type, annotations, options)
            } catch (exception: Throwable) {
                throw IllegalStateException("Unable to autowire '${member.name}' of ${beanDefinition.name}", exception)
            }
            if (value != null || member.returnType.isMarkedNullable) {
                member.isAccessible = true
                member.setter.isAccessible = true
                member.setter.call(beanDefinition.instanceObject, value)
            }
        }
        beanDefinition.propertySet = true
        if (instance is InitializingBean) {
            instance.afterPropertiesSet()
        }
        beanDefinition.instanceObject = beanPostProcessors.fold(beanDefinition.instanceObject) { it, processor ->
            processor.postProcessAfterInitialization(it, beanDefinition.name, this)
        }
        if (enableValidation && beanDefinition.instanceObject != null) {
            for (member in beanDefinition.instanceObject!!::class.memberProperties) {
                val memberEnableValidation = member.findAnnotation<EnableValidation>()?.enabled != false
                if (!memberEnableValidation) continue
                val validators = Validator.fromAnnotations(member.annotations)
                try {
                    val member = member as KProperty1<Any?, Any>
                    member.isAccessible = true
                    val value = member.get(beanDefinition.instanceObject)
                    Validator.check(value, validators, propertyResolver)
                } catch (exception: ValidationException) {
                    throw IllegalStateException(
                        "Validation failed for property '${member.name}' of bean '${beanDefinition.name}'",
                        exception
                    )
                }
            }
        }
    }

    override fun autowire(type: KClass<*>, annotations: List<Annotation>, autowireOptions: AutowireOptions): Any? {
        val valueAnnotation = annotations.find { it is Value } as? Value?
        val qualifierAnnotation = annotations.find { it is Qualifier } as? Qualifier?
        val validators = Validator.fromAnnotations(annotations)

        @Suppress("UNCHECKED_CAST")
        fun check(value: Any?) = value.also {
            Validator.check(value, validators, propertyResolver)
        }

        if (valueAnnotation != null) {
            val text = valueAnnotation.expression
            if (valueAnnotation.isRaw) {
                val value = propertyResolver.resolve(type, text)
                return check(value)
            } else {
                val expression = propertyResolver.parse(text)
                val value = propertyResolver.resolve(type, expression.resolve(propertyResolver))
                return check(value)
            }
        } else {
            if (type.java.isArray) {
                val componentType = type.java.componentType.kotlin
                val beans =
                    if (qualifierAnnotation == null) findBeanDefinitions(componentType).map(::construct) else emptySet()
                val array = Array.newInstance(componentType.java, beans.size)
                if (qualifierAnnotation != null) {
                    Array.set(array, 0, getBean(qualifierAnnotation.name))
                } else {
                    beans.forEachIndexed { index, value -> Array.set(array, index, value) }
                }
                return check(array)
            }
            val bean = runCatching {
                if (qualifierAnnotation != null)
                    findBeanDefinition(qualifierAnnotation.name)
                else findBeanDefinition(type)
            }.getOrNull()?.let { construct(it) }
            return check(bean)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    protected fun collectDependencies(
        dependencyList: DependencyList,
        beanDefinition: BeanDefinition,
        stack: MutableList<BeanDefinition> = mutableListOf()
    ) {
        if (beanDefinition in dependencyList) return
        if (beanDefinition in stack) {
            throw IllegalStateException("Circular dependency detected: ${stack.joinToString(" -> ") { it.name }} -> ${beanDefinition.name}")
        }
        stack += beanDefinition
        val dependencies = mutableListOf<Class<*>>()
        dependencies += beanDefinition.constructor?.parameters?.map { it.type.javaType }?.filterIsInstance<Class<*>>()
            ?: emptyList()
        listOfNotNull(beanDefinition.beanClass, beanDefinition.constructor).mapNotNull {
            findAnnotation(it, Import::class)
        }.forEach {
            dependencies += it.classes.map { clazz -> clazz.java }
        }
        for (type in dependencies) {
            if (type.isArray) {
                for (component in findBeanDefinitions(type.componentType().kotlin)) {
                    collectDependencies(dependencyList, component, stack)
                }
            } else {
                collectDependencies(dependencyList, findBeanDefinition(type.kotlin) ?: continue, stack)
            }
        }
        dependencyList.add(beanDefinition)
    }


    override fun findBeanDefinition(name: String) = beans[name]
    override fun <T : Any> findBeanDefinitions(type: KClass<T>) =
        beans.values.filter { it.beanClass.isSubclassOf(type) }.toSet()

    override fun close() {
        this.beans.values.sortedByDescending { it.order }.forEach {
            if (it.instanceObject == null) return@forEach
            val method = it.getDestroyMethod() ?: return@forEach
            method.isAccessible = true
            method.call(it.instanceObject)
        }
        beans.clear()
    }

    override fun registerSingleton(singleton: Any, name: String?) {
        val beanName = name ?: singleton::class.jvmName
        require(singleton !in beans) { "Duplicate bean name: $name" }
        val beanDefinition = BeanDefinition(beanName, singleton::class, singleton)
        beanDefinition.constructed = true
        beanDefinition.propertySet = true
        beans[beanName] = beanDefinition
    }

}
