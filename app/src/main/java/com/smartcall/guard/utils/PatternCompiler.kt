package com.smartcall.guard.utils

import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import java.util.regex.Pattern

object PatternCompiler {
    data class CompiledPattern(
        val ruleId: String,
        val pattern: Pattern,
        val originalValue: String
    )

    fun compile(rules: List<RuleEntity>): List<CompiledPattern> {
        return rules
            .filter { it.type == RuleType.BLACKLIST_REGEX && it.isActive }
            .map { rule ->
                CompiledPattern(
                    ruleId = rule.id,
                    pattern = Pattern.compile(rule.value),
                    originalValue = rule.value
                )
            }
    }

    fun match(compiledPatterns: List<CompiledPattern>, number: String): CompiledPattern? {
        val normalized = NumberNormalizer.normalize(number)
        for (cp in compiledPatterns) {
            try {
                val startTime = System.currentTimeMillis()
                val isMatch = cp.pattern.matcher(normalized).matches()
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed > 50) continue
                if (isMatch) return cp
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }
}
