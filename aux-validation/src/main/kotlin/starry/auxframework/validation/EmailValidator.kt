package starry.auxframework.validation

import starry.auxframework.context.property.PropertyResolver
import starry.auxframework.context.property.resolve
import starry.auxframework.context.property.validation.ValidationException
import starry.auxframework.context.property.validation.Validator
import starry.auxframework.validation.annotation.Email

object EmailValidator : Validator<Email> {

    override fun validate(value: Any?, configuration: Email, propertyResolver: PropertyResolver) {
        val text = propertyResolver.resolve<String>(value)
        if (text == null) {
            if (configuration.nullable) return
            throw ValidationException("Value cannot be null")
        }
        if (!isEmail(text)) {
            throw ValidationException("Invalid email format: $text")
        }
    }

    fun isEmail(text: String): Boolean {
        // 长度检查 (RFC 5321 要求邮箱最大长度254的字符)
        if (text.isEmpty() || text.length > 254) return false

        // 拆分本地部分和域名
        val parts = text.split('@')
        if (parts.size != 2) return false  // 必须且只能有一个@

        val localPart = parts[0]
        val domain = parts[1]

        // 验证本地部分 (RFC 5322 第3.4.1节)
        if (!isValidLocalPart(localPart)) return false

        // 验证域名 (RFC 5322 第3.4.1节)
        if (!isValidDomain(domain)) return false

        return true
    }

    private fun isValidLocalPart(local: String): Boolean {
        if (local.isEmpty() || local.length > 64) return false

        // 处理带引号的字符串 (如 "user@name")
        if (local.first() == '"' && local.last() == '"') {
            return isValidQuotedString(local.substring(1, local.length - 1))
        }

        // 验证不带引号的本地部分
        var lastChar = '.'
        var dotCount = 0

        for ((index, char) in local.withIndex()) {
            when (char) {
                // 允许的字符集 (RFC 5322 第3.2.3节)
                in 'a'..'z', in 'A'..'Z', in '0'..'9' -> {}
                in "!#$%&'*+-/=?^_`{|}~" -> {}
                '.' -> {
                    // 点不能连续或出现在首尾
                    if (index == 0 || index == local.length - 1 || lastChar == '.') return false
                    dotCount++
                }
                else -> return false  // 非法字符
            }
            lastChar = char
        }

        return dotCount < 2 || local.contains("..").not()
    }

    private fun isValidQuotedString(quoted: String): Boolean {
        var lastChar: Char? = null

        for (char in quoted) {
            when (char) {
                // 允许的转义字符 (RFC 5322 第3.2.1节)
                '\\', '"' -> {
                    // 引号必须转义，转义符不能是最后一个字符
                    if (lastChar == '\\') {
                        lastChar = null
                        continue
                    }
                    if (char == '"' && lastChar != '\\') return false
                }
                // 允许ASCII 32-126范围内的可打印字符
                in '\u0020'..'\u007E' -> {}
                else -> return false  // 非法字符
            }
            lastChar = if (lastChar == '\\') null else char
        }

        // 确保最后一个转义符被处理
        return lastChar != '\\'
    }

    private fun isValidDomain(domain: String): Boolean {
        if (domain.isEmpty() || domain.length > 253) return false

        // 检查IPv6格式 (如 [IPv6:2001:db8::1])
        if (domain.startsWith('[')) {
            return domain.endsWith(']') && isValidIpLiteral(domain.substring(1, domain.length - 1))
        }

        // 验证标准域名 (RFC 1034 第3.5节)
        val labels = domain.split('.')
        if (labels.size < 2) return false  // 至少需要两级域名

        for (label in labels) {
            if (label.isEmpty() || label.length > 63) return false

            // 验证标签字符集 (RFC 3696 第2节)
            if (!label.all { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '-' }) {
                return false
            }

            // 连字符不能在开头或结尾 (RFC 3696 第2节)
            if (label.startsWith('-') || label.endsWith('-')) return false
        }

        return true
    }

    private fun isValidIpLiteral(ip: String): Boolean {
        // 简化版IP验证 (实际实现应更严谨)
        return when {
            ip.startsWith("IPv6:") -> isValidIPv6(ip.substring(5))
            ip.contains('.') -> isValidIPv4(ip)
            else -> false
        }
    }

    private fun isValidIPv4(ip: String): Boolean {
        val octets = ip.split('.')
        if (octets.size != 4) return false

        return octets.all { octet ->
            octet.toIntOrNull()?.let { it in 0..255 } ?: false
        }
    }

    private fun isValidIPv6(ip: String): Boolean {
        // 简化版IPv6验证
        val groups = ip.split(':')
        if (groups.size < 3 || groups.size > 8) return false

        var emptyCount = 0
        for (group in groups) {
            if (group.isEmpty()) {
                emptyCount++
                continue
            }
            if (!group.all { it in '0' .. '9' || it in 'a' .. 'f' || it in 'A' .. 'F' }) return false
            if (group.length > 4) return false
        }

        return emptyCount <= 1
    }

}
