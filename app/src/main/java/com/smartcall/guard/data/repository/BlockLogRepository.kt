package com.smartcall.guard.data.repository

import com.smartcall.guard.data.dao.BlockLogDao
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
}
