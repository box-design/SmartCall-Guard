package com.smartcall.guard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartcall.guard.data.entity.RuleType
import java.util.UUID

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: RuleType,
    val value: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
