package com.smartcall.guard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartcall.guard.data.entity.LocationRule

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val locationRule: LocationRule = LocationRule.OFF,
    val blockUnknownSegment: Boolean = false,
    val whitelistContacts: Boolean = true,
    val whitelistRecentOutgoing: Boolean = false,
    val maxRadiusKm: Int = 50,
    val cacheLocationMinutes: Int = 30
)
