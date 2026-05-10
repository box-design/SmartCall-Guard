package com.smartcall.guard.data.database

import androidx.room.TypeConverter
import com.smartcall.guard.data.entity.BlockMode
import com.smartcall.guard.data.entity.BlockReason
import com.smartcall.guard.data.entity.LocationRule
import com.smartcall.guard.data.entity.RuleType

class Converters {
    @TypeConverter
    fun fromRuleType(value: RuleType): String = value.name

    @TypeConverter
    fun toRuleType(value: String): RuleType = try {
        RuleType.valueOf(value)
    } catch (_: IllegalArgumentException) {
        RuleType.BLACKLIST_EXACT
    }

    @TypeConverter
    fun fromBlockReason(value: BlockReason): String = value.name

    @TypeConverter
    fun toBlockReason(value: String): BlockReason = try {
        BlockReason.valueOf(value)
    } catch (_: IllegalArgumentException) {
        BlockReason.BLACKLIST
    }

    @TypeConverter
    fun fromLocationRule(value: LocationRule): String = value.name

    @TypeConverter
    fun toLocationRule(value: String): LocationRule = try {
    LocationRule.valueOf(value)
} catch (_: IllegalArgumentException) {
    LocationRule.OFF
}

    @TypeConverter
    fun fromBlockMode(value: BlockMode): String = value.name

    @TypeConverter
    fun toBlockMode(value: String): BlockMode = try {
        BlockMode.valueOf(value)
    } catch (_: IllegalArgumentException) {
        BlockMode.NORMAL
    }
}
