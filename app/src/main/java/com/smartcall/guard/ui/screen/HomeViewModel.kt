package com.smartcall.guard.ui.screen

import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcall.guard.data.entity.BlockMode
import com.smartcall.guard.data.repository.BlockLogRepository
import com.smartcall.guard.data.repository.SettingsRepository
import com.smartcall.guard.service.GuardForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: android.content.Context,
    private val blockLogRepository: BlockLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _todayBlockCount = MutableStateFlow(0)
    val todayBlockCount: StateFlow<Int> = _todayBlockCount.asStateFlow()

    private val _isServiceEnabled = MutableStateFlow(true)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _blockMode = MutableStateFlow(BlockMode.NORMAL)
    val blockMode: StateFlow<BlockMode> = _blockMode.asStateFlow()

    private val _nightModeEnabled = MutableStateFlow(false)
    val nightModeEnabled: StateFlow<Boolean> = _nightModeEnabled.asStateFlow()

    init {
        loadTodayBlockCount()
        loadSettings()
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

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _blockMode.value = it.blockMode
                    _nightModeEnabled.value = it.nightModeEnabled
                }
            }
        }
    }

    fun toggleService(enabled: Boolean) {
        _isServiceEnabled.value = enabled
        val serviceIntent = Intent(applicationContext, GuardForegroundService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(applicationContext, serviceIntent)
            } else {
                applicationContext.startService(serviceIntent)
            }
        } else {
            applicationContext.stopService(serviceIntent)
        }
    }
}
