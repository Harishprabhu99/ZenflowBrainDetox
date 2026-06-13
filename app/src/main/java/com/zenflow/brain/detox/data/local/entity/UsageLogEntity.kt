package com.zenflow.brain.detox.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_logs",
    indices = [Index(value = ["packageName", "dateKey"], unique = true)],
)
data class UsageLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val dateKey: String,
    val usedMs: Long,
    val openCount: Int,
)
