package com.smartcall.guard.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.smartcall.guard.data.dao.BlockLogDao
import com.smartcall.guard.data.dao.RuleDao
import com.smartcall.guard.data.dao.SettingsDao
import com.smartcall.guard.data.entity.BlockLogEntity
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.SettingsEntity

@Database(
    entities = [RuleEntity::class, BlockLogEntity::class, SettingsEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun blockLogDao(): BlockLogDao
    abstract fun settingsDao(): SettingsDao
}
