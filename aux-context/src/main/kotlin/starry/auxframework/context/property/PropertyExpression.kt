package starry.auxframework.context.property

import starry.auxframework.AuxFramework

interface PropertyExpression {

    fun resolve(properties: PropertyResolver): Any?

}

class ConstantPropertyExpression(private val value: Any?, private val raw: String = value.toString()) :
    PropertyExpression {

    override fun resolve(properties: PropertyResolver): Any? {
        return value
    }

    override fun toString() = raw

}

class SimplePropertyExpression(private val key: String, private val default: PropertyExpression? = null) :
    PropertyExpression {

    override fun resolve(properties: PropertyResolver): Any? {
        return properties[key] ?: default?.resolve(properties)
    }

    override fun toString(): String {
        return if (default != null) {
            "\${$key:$default}"
        } else {
            "\${$key}"
        }
    }

}

class CallPropertyExpression(private val name: String, private val arguments: List<PropertyExpression>) :
    PropertyExpression {

    override fun resolve(properties: PropertyResolver): Any? {
        return properties.call(name, arguments)
            ?: throw IllegalArgumentException("Function '$name' is not defined.")
    }

    override fun toString(): String {
        return "#$name(${arguments.joinToString(", ")})"
    }

}

class RunningArgumentPropertyExpression(private val index: Int) : PropertyExpression {

    override fun resolve(properties: PropertyResolver): Any? {
        if (index < 0 || index > AuxFramework.arguments.size) {
            throw IndexOutOfBoundsException("Argument index $index is out of bounds")
        }
        return if (index == 0) AuxFramework.arguments
        else AuxFramework.arguments[index - 1]
    }

    override fun toString(): String {
        return "$$index"
    }

}
