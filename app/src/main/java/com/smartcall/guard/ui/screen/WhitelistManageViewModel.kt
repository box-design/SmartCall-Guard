package com.smartcall.guard.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import com.smartcall.guard.data.repository.RuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhitelistManageViewModel @Inject constructor(
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _rules = MutableStateFlow<List<RuleEntity>>(emptyList())
    val rules: StateFlow<List<RuleEntity>> = _rules.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _selectedIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        viewModelScope.launch {
            ruleRepository.getRulesByType(RuleType.WHITELIST).collect { rules ->
                _rules.value = rules
            }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(id: String) {
        _selectedIds.value = if (id in _selectedIds.value) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
    }

    fun selectAll() {
        _selectedIds.value = getFilteredRules().map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                ruleRepository.deleteRule(id)
            }
            _selectedIds.value = emptySet()
        }
    }

    fun toggleActive(rule: RuleEntity) {
        viewModelScope.launch {
            ruleRepository.updateRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun deleteRule(id: String) {
        viewModelScope.launch {
            ruleRepository.deleteRule(id)
        }
    }

    fun addWhitelistRule(value: String, note: String?, tag: String?) {
        viewModelScope.launch {
            ruleRepository.insertRule(
                RuleEntity(type = RuleType.WHITELIST, value = value, note = note, tag = tag)
            )
        }
    }

    fun getFilteredRules(): List<RuleEntity> {
        return _rules.value.filter { rule ->
            _searchQuery.value.isBlank() ||
                    rule.value.contains(_searchQuery.value, ignoreCase = true) ||
                    (rule.tag?.contains(_searchQuery.value, ignoreCase = true) == true) ||
                    (rule.note?.contains(_searchQuery.value, ignoreCase = true) == true)
        }
    }
}
