package com.smartcall.guard.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcall.guard.data.entity.BlockLogEntity
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.data.repository.BlockLogRepository
import com.smartcall.guard.data.repository.RuleRepository
import com.smartcall.guard.utils.NumberNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val blockLogRepository: BlockLogRepository,
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<BlockLogEntity>>(emptyList())
    val logs: StateFlow<List<BlockLogEntity>> = _logs.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            blockLogRepository.getAllLogs().collect { logList ->
                _logs.value = logList
            }
        }
    }

    fun addToWhitelist(log: BlockLogEntity) {
        viewModelScope.launch {
            val rule = RuleEntity(
                type = RuleType.WHITELIST,
                value = log.phoneNumber,
                note = "从拦截记录添加"
            )
            ruleRepository.insertRule(rule)
        }
    }
}
