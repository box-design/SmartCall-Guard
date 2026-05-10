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
class RulesViewModel @Inject constructor(
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _rules = MutableStateFlow<List<RuleEntity>>(emptyList())
    val rules: StateFlow<List<RuleEntity>> = _rules.asStateFlow()

    private val _selectedFilter = MutableStateFlow<RuleType?>(null)
    val selectedFilter: StateFlow<RuleType?> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _filteredRules = MutableStateFlow<List<RuleEntity>>(emptyList())
    val filteredRules: StateFlow<List<RuleEntity>> = _filteredRules.asStateFlow()

    init {
        loadRules()
    }

    private fun loadRules() {
        viewModelScope.launch {
            ruleRepository.getAllRules().collect { ruleList ->
                _rules.value = ruleList
                updateFilteredRules()
            }
        }
    }

    fun filterByType(type: RuleType?) {
        _selectedFilter.value = type
        updateFilteredRules()
    }

    fun search(query: String) {
        _searchQuery.value = query
        updateFilteredRules()
    }

    private fun updateFilteredRules() {
        val filtered = _rules.value.filter { rule ->
            val typeMatch = _selectedFilter.value?.let { filterType ->
                when (filterType) {
                    RuleType.BLACKLIST_EXACT -> rule.type in listOf(RuleType.BLACKLIST_EXACT, RuleType.BLACKLIST_PREFIX, RuleType.BLACKLIST_REGEX)
                    else -> rule.type == filterType
                }
            } ?: true

            val queryMatch = _searchQuery.value.isBlank() ||
                    rule.value.contains(_searchQuery.value, ignoreCase = true) ||
                    (rule.note?.contains(_searchQuery.value, ignoreCase = true) == true)

            typeMatch && queryMatch
        }
        _filteredRules.value = filtered
    }

    fun saveRule(type: RuleType, value: String, note: String?, existingId: String? = null) {
        viewModelScope.launch {
            val rule = existingId?.let { ruleRepository.getRuleByIdSync(it) } ?: RuleEntity(type = type, value = value, note = note)
            ruleRepository.insertRule(rule.copy(type = type, value = value, note = note))
        }
    }

    fun deleteRule(id: String) {
        viewModelScope.launch {
            ruleRepository.deleteRule(id)
        }
    }
}
