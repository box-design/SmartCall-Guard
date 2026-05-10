package com.smartcall.guard.di

import android.content.Context
import androidx.room.Room
import com.smartcall.guard.data.dao.BlockLogDao
import com.smartcall.guard.data.dao.RuleDao
import com.smartcall.guard.data.dao.SettingsDao
import com.smartcall.guard.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideRuleDao(database: AppDatabase): RuleDao = database.ruleDao()

    @Provides
    fun provideBlockLogDao(database: AppDatabase): BlockLogDao = database.blockLogDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()
}
