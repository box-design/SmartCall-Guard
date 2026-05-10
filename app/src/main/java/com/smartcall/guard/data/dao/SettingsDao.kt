package com.smartcall.guard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcall.guard.data.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsSync(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)
}
