package com.smartcall.guard.data.repository

import com.smartcall.guard.data.dao.BlockLogDao
import com.smartcall.guard.data.dao.NumberStat
import com.smartcall.guard.data.dao.ReasonStat
import com.smartcall.guard.data.entity.BlockLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlockLogRepository @Inject constructor(
    private val blockLogDao: BlockLogDao
) {

    fun getAllLogs(): Flow<List<BlockLogEntity>> = blockLogDao.getAllLogs()

    fun getTodayBlockCount(startOfDay: Long): Flow<Int> = blockLogDao.getTodayBlockCount(startOfDay)

    suspend fun insertLog(log: BlockLogEntity) = blockLogDao.insert(log)

    fun insertLogSync(log: BlockLogEntity) = blockLogDao.insertSync(log)

    suspend fun deleteLog(id: String) = blockLogDao.deleteById(id)

    fun getBlockStatsByReason(startTime: Long): Flow<List<ReasonStat>> =
        blockLogDao.getBlockStatsByReason(startTime)

    fun getBlockCountInRange(startTime: Long, endTime: Long): Flow<Int> =
        blockLogDao.getBlockCountInRange(startTime, endTime)

    fun getTopBlockedNumbers(startTime: Long, limit: Int = 10): Flow<List<NumberStat>> =
        blockLogDao.getTopBlockedNumbers(startTime, limit)

    fun getRecentBlockedNumbersSync(cutoffTime: Long): List<String> =
        blockLogDao.getRecentBlockedNumbersSync(cutoffTime)
}
