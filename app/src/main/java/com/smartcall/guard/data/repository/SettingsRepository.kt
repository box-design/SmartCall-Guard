package com.smartcall.guard.data.repository

import com.smartcall.guard.data.dao.SettingsDao
import com.smartcall.guard.data.entity.SettingsEntity
import com.smartcall.guard.data.entity.LocationRule
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {

    fun getSettings(): Flow<SettingsEntity?> = settingsDao.getSettings()

    suspend fun upsertSettings(settings: SettingsEntity) = settingsDao.upsert(settings)

    fun getSettingsSync(): SettingsEntity? = settingsDao.getSettingsSync()

    fun getDefaultSettings(): SettingsEntity = SettingsEntity(
        id = 1,
        locationRule = LocationRule.OFF,
        blockUnknownSegment = false,
        whitelistContacts = true,
        whitelistRecentOutgoing = false,
        maxRadiusKm = 50,
        cacheLocationMinutes = 30
    )
}
