package com.smartcall.guard.data.entity

enum class BlockMode {
    NORMAL,
    STRICT,
    WHITELIST_ONLY;

    fun displayName(): String = when (this) {
        NORMAL -> "普通模式"
        STRICT -> "严格模式"
        WHITELIST_ONLY -> "仅白名单"
    }

    fun displayDesc(): String = when (this) {
        NORMAL -> "仅拦截黑名单和归属地规则匹配的来电"
        STRICT -> "拦截所有非白名单来电"
        WHITELIST_ONLY -> "只允许白名单和通讯录中的号码"
    }
}
