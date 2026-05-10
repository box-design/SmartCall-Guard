package com.smartcall.guard.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcall.guard.data.repository.BlockLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val blockLogRepository: BlockLogRepository
) : ViewModel() {

    private val _todayBlockCount = MutableStateFlow(0)
    val todayBlockCount: StateFlow<Int> = _todayBlockCount.asStateFlow()

    private val _isServiceEnabled = MutableStateFlow(true)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    init {
        loadTodayBlockCount()
    }

    private fun loadTodayBlockCount() {
        viewModelScope.launch {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            blockLogRepository.getTodayBlockCount(startOfDay).collect { count ->
                _todayBlockCount.value = count
            }
        }
    }

    fun toggleService(enabled: Boolean) {
        _isServiceEnabled.value = enabled
    }
}
