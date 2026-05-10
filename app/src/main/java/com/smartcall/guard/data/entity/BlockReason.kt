package com.smartcall.guard.data.entity

enum class BlockReason {
    BLACKLIST,
    LOCATION_MISMATCH,
    CLOUD_MARKED,
    UNKNOWN_SEGMENT,
    NIGHT_MODE,
    STRICT_MODE,
    SEGMENT_BLOCK;

    fun displayName(): String = when (this) {
        BLACKLIST -> "黑名单"
        LOCATION_MISMATCH -> "归属地不匹配"
        CLOUD_MARKED -> "云端标记"
        UNKNOWN_SEGMENT -> "未知归属地"
        NIGHT_MODE -> "夜间模式"
        STRICT_MODE -> "严格模式"
        SEGMENT_BLOCK -> "号段拦截"
    }
}
