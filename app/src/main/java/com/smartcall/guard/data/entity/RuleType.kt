package com.smartcall.guard.data.entity

enum class RuleType {
    BLACKLIST_EXACT,
    BLACKLIST_PREFIX,
    BLACKLIST_REGEX,
    BLACKLIST_SEGMENT,
    WHITELIST;

    fun displayName(): String = when (this) {
        BLACKLIST_EXACT -> "精确匹配"
        BLACKLIST_PREFIX -> "前缀匹配"
        BLACKLIST_REGEX -> "正则匹配"
        BLACKLIST_SEGMENT -> "号段拦截"
        WHITELIST -> "白名单"
    }

    fun isBlacklist(): Boolean = this in listOf(BLACKLIST_EXACT, BLACKLIST_PREFIX, BLACKLIST_REGEX, BLACKLIST_SEGMENT)
}
