package com.smartcall.guard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val locationRule: LocationRule = LocationRule.OFF,
    val blockUnknownSegment: Boolean = false,
    val whitelistContacts: Boolean = true,
    val whitelistRecentOutgoing: Boolean = false,
    val maxRadiusKm: Int = 50,
    val cacheLocationMinutes: Int = 30,
    val nightModeEnabled: Boolean = false,
    val nightModeStart: String = "22:00",
    val nightModeEnd: String = "07:00",
    val repeatCallPassEnabled: Boolean = false,
    val repeatCallPassMinutes: Int = 3,
    val blockMode: BlockMode = BlockMode.NORMAL
)
