package com.smartcall.guard.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcall.guard.data.entity.SettingsEntity
import com.smartcall.guard.data.repository.SettingsRepository
import com.smartcall.guard.domain.usecase.ImportContactsUseCase
import com.smartcall.guard.domain.usecase.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val importContactsUseCase: ImportContactsUseCase
) : ViewModel() {

    private val _settings = MutableStateFlow<SettingsEntity?>(null)
    val settings: StateFlow<SettingsEntity?> = _settings.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _privacyConsent = MutableStateFlow(false)
    val privacyConsent: StateFlow<Boolean> = _privacyConsent.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { setting ->
                _settings.value = setting ?: settingsRepository.getDefaultSettings()
            }
        }
    }

    fun updateSettings(settings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepository.upsertSettings(settings)
        }
    }

    fun importContacts() {
        viewModelScope.launch {
            val result = importContactsUseCase.execute()
            _importResult.value = result
        }
    }

    fun acceptPrivacy() {
        _privacyConsent.value = true
    }
}
