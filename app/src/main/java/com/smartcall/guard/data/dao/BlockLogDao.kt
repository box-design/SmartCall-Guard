package com.smartcall.guard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcall.guard.data.entity.BlockLogEntity
import com.smartcall.guard.data.entity.BlockReason
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

    @Query("SELECT blockReason, COUNT(*) as count FROM block_logs WHERE timestamp >= :startTime GROUP BY blockReason")
    fun getBlockStatsByReason(startTime: Long): Flow<List<ReasonStat>>

    @Query("SELECT COUNT(*) FROM block_logs WHERE timestamp >= :startTime AND timestamp < :endTime")
    fun getBlockCountInRange(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT phoneNumber, COUNT(*) as count FROM block_logs WHERE timestamp >= :startTime GROUP BY phoneNumber ORDER BY count DESC LIMIT :limit")
    fun getTopBlockedNumbers(startTime: Long, limit: Int = 10): Flow<List<NumberStat>>

    @Query("SELECT DISTINCT normalizedNumber FROM block_logs WHERE timestamp >= :cutoffTime")
    fun getRecentBlockedNumbersSync(cutoffTime: Long): List<String>
}

data class ReasonStat(
    val blockReason: BlockReason,
    val count: Int
)

data class NumberStat(
    val phoneNumber: String,
    val count: Int
)
