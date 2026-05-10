package com.smartcall.guard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartcall.guard.data.entity.RuleEntity
import com.smartcall.guard.data.entity.RuleType
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE type = :type")
    fun getRulesByType(type: RuleType): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE type = :type AND isActive = 1")
    fun getActiveRulesByType(type: RuleType): Flow<List<RuleEntity>>

    @Query("SELECT * FROM rules WHERE id = :id")
    fun getRuleById(id: String): Flow<RuleEntity?>

    @Query("SELECT * FROM rules WHERE id = :id")
    suspend fun getRuleByIdSync(id: String): RuleEntity?

    @Query("SELECT * FROM rules WHERE type = :type AND isActive = 1")
    fun getActiveRulesByTypeSync(type: RuleType): List<RuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RuleEntity>)

    @Update
    suspend fun update(rule: RuleEntity)

    @Query("DELETE FROM rules WHERE id = :id")
    suspend fun deleteById(id: String)
}
