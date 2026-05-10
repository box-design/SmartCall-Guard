package com.smartcall.guard.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcall.guard.data.dao.NumberStat
import com.smartcall.guard.data.dao.ReasonStat
import com.smartcall.guard.data.repository.BlockLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class BlockStatsViewModel @Inject constructor(
    private val blockLogRepository: BlockLogRepository
) : ViewModel() {

    private val _todayCount = MutableStateFlow(0)
    val todayCount: StateFlow<Int> = _todayCount.asStateFlow()

    private val _weekCount = MutableStateFlow(0)
    val weekCount: StateFlow<Int> = _weekCount.asStateFlow()

    private val _monthCount = MutableStateFlow(0)
    val monthCount: StateFlow<Int> = _monthCount.asStateFlow()

    private val _reasonStats = MutableStateFlow<List<ReasonStat>>(emptyList())
    val reasonStats: StateFlow<List<ReasonStat>> = _reasonStats.asStateFlow()

    private val _topNumbers = MutableStateFlow<List<NumberStat>>(emptyList())
    val topNumbers: StateFlow<List<NumberStat>> = _topNumbers.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -7)
        val startOfWeek = cal.timeInMillis

        cal.timeInMillis = now
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val startOfMonth = cal.timeInMillis

        viewModelScope.launch {
            blockLogRepository.getBlockCountInRange(startOfDay, now).collect { _todayCount.value = it }
        }
        viewModelScope.launch {
            blockLogRepository.getBlockCountInRange(startOfWeek, now).collect { _weekCount.value = it }
        }
        viewModelScope.launch {
            blockLogRepository.getBlockCountInRange(startOfMonth, now).collect { _monthCount.value = it }
        }
        viewModelScope.launch {
            blockLogRepository.getBlockStatsByReason(startOfMonth).collect { _reasonStats.value = it }
        }
        viewModelScope.launch {
            blockLogRepository.getTopBlockedNumbers(startOfMonth, 10).collect { _topNumbers.value = it }
        }
    }
}
