package com.zenflow.brain.detox.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenflow.brain.detox.data.local.entity.UsageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs WHERE dateKey = :dateKey")
    fun observeForDate(dateKey: String): Flow<List<UsageLogEntity>>

    @Query("SELECT * FROM usage_logs WHERE packageName = :packageName AND dateKey = :dateKey LIMIT 1")
    suspend fun getForPackageAndDate(packageName: String, dateKey: String): UsageLogEntity?

    @Query("SELECT * FROM usage_logs WHERE dateKey BETWEEN :startDate AND :endDate")
    suspend fun getBetweenDates(startDate: String, endDate: String): List<UsageLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: UsageLogEntity): Long

    @Query("DELETE FROM usage_logs WHERE dateKey < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String): Int
}
