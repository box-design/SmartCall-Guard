package com.smartcall.guard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartcall.guard.data.entity.BlockReason
import java.util.UUID

@Entity(tableName = "block_logs")
data class BlockLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val normalizedNumber: String,
    val displayLocation: String? = null,
    val matchedRule: String? = null,
    val blockReason: BlockReason,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
