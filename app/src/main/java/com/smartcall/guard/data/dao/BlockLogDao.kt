package com.smartcall.guard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcall.guard.data.entity.BlockLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockLogDao {

    @Query("SELECT * FROM block_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<BlockLogEntity>>

    @Query("SELECT COUNT(*) FROM block_logs WHERE timestamp >= :startOfDay")
    fun getTodayBlockCount(startOfDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: BlockLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(log: BlockLogEntity)

    @Query("DELETE FROM block_logs WHERE id = :id")
    suspend fun deleteById(id: String)
}
