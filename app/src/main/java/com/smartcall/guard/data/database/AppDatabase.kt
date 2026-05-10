package com.smartcall.guard.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartcall.guard.data.dao.BlockLogDao
import com.smartcall.guard.data.dao.RuleDao
import com.smartcall.guard.data.dao.SettingsDao
import com.smartcall.guard.data.entity.BlockLogEntity
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.SettingsEntity

@Database(
    entities = [RuleEntity::class, BlockLogEntity::class, SettingsEntity::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ruleDao(): RuleDao
    abstract fun blockLogDao(): BlockLogDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE rules ADD COLUMN tag TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE settings ADD COLUMN nightModeEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN nightModeStart TEXT NOT NULL DEFAULT '22:00'")
                db.execSQL("ALTER TABLE settings ADD COLUMN nightModeEnd TEXT NOT NULL DEFAULT '07:00'")
                db.execSQL("ALTER TABLE settings ADD COLUMN repeatCallPassEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE settings ADD COLUMN repeatCallPassMinutes INTEGER NOT NULL DEFAULT 3")
                db.execSQL("ALTER TABLE settings ADD COLUMN blockMode TEXT NOT NULL DEFAULT 'NORMAL'")
            }
        }
    }
}
