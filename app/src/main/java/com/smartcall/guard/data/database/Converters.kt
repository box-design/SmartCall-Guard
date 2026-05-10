package com.smartcall.guard.data.database

import androidx.room.TypeConverter
import com.smartcall.guard.data.entity.BlockReason
import com.smartcall.guard.data.entity.LocationRule
import com.smartcall.guard.data.entity.RuleType

class Converters {
    @TypeConverter
    fun fromRuleType(value: RuleType): String = value.name

    @TypeConverter
    fun toRuleType(value: String): RuleType = RuleType.valueOf(value)

    @TypeConverter
    fun fromBlockReason(value: BlockReason): String = value.name

    @TypeConverter
    fun toBlockReason(value: String): BlockReason = BlockReason.valueOf(value)

    @TypeConverter
    fun fromLocationRule(value: LocationRule): String = value.name

    @TypeConverter
    fun toLocationRule(value: String): LocationRule = LocationRule.valueOf(value)
}
